/* Copyright (c) 2024 openEuler Community
 EasySoftwareInput is licensed under the Mulan PSL v2.
 You can use this software according to the terms and conditions of the Mulan PSL v2.
 You may obtain a copy of Mulan PSL v2 at:
     http://license.coscl.org.cn/MulanPSL2
 THIS SOFTWARE IS PROVIDED ON AN "AS IS" BASIS, WITHOUT WARRANTIES OF ANY KIND,
 EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO NON-INFRINGEMENT,
 MERCHANTABILITY OR FIT FOR A PARTICULAR PURPOSE.
 See the Mulan PSL v2 for more details.
*/
package com.easysoftwareinput.common.dag;

import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.Queue;

public class DagTaskExecutor {
    /**
     * logger.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(DagTaskExecutor.class);
    /**
     * beanTasks.
     */
    private Map<String, Object> beanTasks = new HashMap<>();
    /**
     * number of task registerd.
     */
    private Integer numTask = 0;
    /**
     * indegree of each task.
     */
    private Map<String, Integer> inDegree = new HashMap<>();
    /**
     * graph dependency node.
     */
    private Map<String, List<String>> pointTo = new HashMap<>();
    /**
     * graph.
     */
    private Map<String, List<String>> graph = new HashMap<>();
    /**
     * tasks.
     */
    private Map<String, Supplier<String>> tasks = new HashMap<>();

    /**
     * Add task object into dag.
     *
     * @param name the name of task.
     * @param task task obejct that running the program.
     */
    public void addTaskObj(String name, Object task) {
        if (task == null) {
            throw new IllegalArgumentException("Task cannot be null");
        }
        Object existingTask = beanTasks.putIfAbsent(name, task);
        if (existingTask == null) {
            numTask++;
        } else {
            LOGGER.info("Task with name '" + name + "' already exists. It was not replaced.");
        }

    }

    /**
     * Add dependency into dag.
     *
     * @param fromTask preceding task.
     * @param toTask   task.
     */
    public void addDependency(String fromTask, String toTask) {

        if (!inDegree.containsKey(fromTask)) {
            inDegree.put(fromTask, 0);
        }

        if (inDegree.containsKey(toTask)) {
            Integer val = inDegree.get(toTask);
            inDegree.put(toTask, val + 1);
        } else {
            inDegree.put(toTask, 1);
        }

        pointTo.computeIfAbsent(fromTask, k -> new ArrayList<>()).add(toTask);
    }

    /**
     * add task Dependency.
     *
     * @param fromTask preceding task.
     * @param toTask   task.
     */
    public void addDependencyGraph(String fromTask, String toTask) {
        graph.computeIfAbsent(fromTask, k -> new ArrayList<>()).add(toTask);
    }

    /**
     * add Task.
     *
     * @param taskName name.
     * @param task     task.
     */
    public void addTask(String taskName, Supplier<String> task) {
        if (task == null) {
            throw new IllegalArgumentException("Task cannot be null");
        }

        Object existingTask = tasks.putIfAbsent(taskName, task);
        if (existingTask != null) {
            LOGGER.info("Task with name '" + taskName + "' already exists. It was not replaced.");
        }
    }

    /**
     * Sequentially execute dag graph.
     *
     */
    public void executeSequentially() throws Exception {
        ExecutorService executor = Executors.newCachedThreadPool();
        Map<String, CompletableFuture<String>> futureTasks = new HashMap<>();
        List<String> order = topologicalSort();

        if (order.isEmpty()) {
            LOGGER.error("error dag,check your graph, circle exsited, plz rearrange");
            return;
        }

        for (String taskName : order) {
            Supplier<String> task = tasks.get(taskName);
            List<CompletableFuture<String>> dependencies = new ArrayList<>();

            // 添加当前任务的所有依赖作为前置任务
            for (String dependency : graph.getOrDefault(taskName, Collections.emptyList())) {
                dependencies.add(futureTasks.get(dependency));
            }

            // 如果当前任务没有依赖，则直接执行
            if (dependencies.isEmpty()) {
                futureTasks.put(taskName, CompletableFuture.supplyAsync(task, executor));
            } else {
                // 如果有依赖，则等待所有依赖完成后执行
                CompletableFuture<Void> allDependencies = CompletableFuture
                        .allOf(dependencies.toArray(new CompletableFuture[0]));
                futureTasks.put(taskName, allDependencies.thenApply(v -> task.get()));
            }
        }

        // 等待所有任务完成
        CompletableFuture.allOf(futureTasks.values().toArray(new CompletableFuture[0])).join();

        // 关闭执行器
        executor.shutdown();
    }

    /**
     * topologicalSort.
     *
     * @return list of services.
     */
    public List<String> topologicalSort() {

        if (!canFinish()) {
            LOGGER.error("error dag,check your graph, circle exsited, plz rearrange");
            return Collections.emptyList();
        }
        Queue<String> queue = new LinkedList<String>();
        Map<String, Integer> sortIndegree = new HashMap<>(inDegree);

        for (Map.Entry<String, Integer> entry : sortIndegree.entrySet()) {
            if (entry.getValue() == 0) {
                queue.add(entry.getKey());
            }
        }

        List<String> layerTask = new ArrayList<>();

        while (!queue.isEmpty()) {
            int curLayerSize = queue.size();

            while (curLayerSize > 0) {

                String task = queue.poll();
                layerTask.add(task);
                List<String> toTasks = pointTo.get(task);
                if (toTasks != null) {
                    for (String toTask : toTasks) {
                        int newValue = sortIndegree.get(toTask) - 1;
                        sortIndegree.put(toTask, newValue);
                        if (newValue == 0) {
                            queue.add(toTask);
                        }
                    }
                }
                curLayerSize--;
            }
        }

        return layerTask;
    }

    /**
     * execute task concurrentlly by bfs topologicalsort.
     *
     */
    public void executeConcurrent() {

        if (!canFinish()) {
            LOGGER.error("error dag,check your graph, circle exsited, plz rearrange");
            return;
        }

        Queue<String> queue = new LinkedList<String>();
        Map<String, Integer> sortIndegree = new HashMap<>(inDegree);

        for (Map.Entry<String, Integer> entry : sortIndegree.entrySet()) {
            if (entry.getValue() == 0) {
                queue.add(entry.getKey());
            }
        }

        while (!queue.isEmpty()) {
            int curLayerSize = queue.size();

            List<String> layerTask = new LinkedList<>();
            while (curLayerSize > 0) {

                String task = queue.poll();
                layerTask.add(task);
                List<String> toTasks = pointTo.get(task);
                if (toTasks != null) {
                    for (String toTask : toTasks) {
                        int newValue = sortIndegree.get(toTask) - 1;
                        sortIndegree.put(toTask, newValue);
                        if (newValue == 0) {
                            queue.add(toTask);
                        }
                    }
                }
                curLayerSize--;
            }

            // 层级并发执行
            if (layerTask.isEmpty()) {
                continue;
            }

            List<CompletableFuture<Void>> futureTask = new ArrayList<>();
            for (String taskName : layerTask) {
                LOGGER.info("cur running task: " + taskName);
                if (beanTasks.containsKey(taskName)) {
                    Object taskObj = beanTasks.get(taskName);
                    futureTask.add(CompletableFuture.runAsync(() -> {
                        try {
                            Class<?> taskObjClass = taskObj.getClass();
                            Method runMethod = taskObjClass.getMethod("run");
                            runMethod.invoke(taskObj);
                        } catch (NoSuchMethodException e) {
                            LOGGER.error("Method 'run' not found in " + taskObj.getClass().getName());
                        } catch (IllegalAccessException e) {
                            // 如果run方法不可访问，则抛出异常
                            LOGGER.error("Method 'run' is not accessible in " + taskObj.getClass().getName());
                        } catch (InvocationTargetException e) {
                            // 如果调用run方法时抛出异常，则捕获并处理
                            LOGGER.error(
                                    "Exception thrown when invoking 'run' method in " + taskObj.getClass().getName());
                        }
                    }));
                }
            }

            for (CompletableFuture<Void> task : futureTask) {
                task.join();
            }
            futureTask.clear();
        }
    }

    /**
     * whether cuurent dag graph is able to excute.
     *
     * @return whether dag graph exsit circle.
     */
    private boolean canFinish() {

        Queue<String> queue = new LinkedList<String>();
        Map<String, Integer> sortIndegree = new HashMap<>(inDegree);

        for (Map.Entry<String, Integer> entry : sortIndegree.entrySet()) {
            if (entry.getValue() == 0) {
                queue.add(entry.getKey());
            }
        }

        int cnt = 0;
        while (!queue.isEmpty()) {
            int curLayerSize = queue.size();

            while (curLayerSize > 0) {
                String task = queue.poll();
                cnt++;
                List<String> toTasks = pointTo.get(task);
                if (toTasks != null) {
                    for (String toTask : toTasks) {
                        int newValue = sortIndegree.get(toTask) - 1;
                        sortIndegree.put(toTask, newValue);
                        if (newValue == 0) {
                            queue.add(toTask);
                        }
                    }
                }
                curLayerSize--;
            }
        }

        return cnt == numTask;
    }
}
