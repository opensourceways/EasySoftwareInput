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
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import org.springframework.data.util.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.util.ArrayList;
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
     * number of task registerd.
     */
    private boolean dependencyCheck = false;
    /**
     * indegree of each task.
     */
    private Map<String, Integer> inDegree = new HashMap<>();
    /**
     * graph dependency node.
     */
    private Map<String, List<String>> pointTo = new HashMap<>();
    /**
     * dag running mode.
     */
    private RunningMode dagRunningMode;

    /**
     * Running Mode of dag task.
     */
    public enum RunningMode {
        /**
         * running all tasks no matters task failed.
         *
         */
        NON_INTERRUP,

        /**
         * one task running failed stop running proceeding tasks.
         *
         */
        INTERRUP
    }

    /**
     * set running mode.
     *
     * @param runningMode
     */
    public DagTaskExecutor(RunningMode runningMode) {
        this.dagRunningMode = runningMode;
    }

    /**
     * default constructor with INTERRUP mode.
     */
    public DagTaskExecutor() {
        this.dagRunningMode = RunningMode.INTERRUP;
    }

    /**
     * Add task object into dag.
     *
     * @param name the name of task.
     * @param task task obejct that running the program.
     */
    public void addTaskObj(String name, Object task) {
        // 空值检查
        if (task == null) {
            throw new IllegalArgumentException("Task cannot be null");
        }
        // 方法检查
        try {
            Method method = task.getClass().getMethod("run");
        } catch (NoSuchMethodException e) {
            throw new IllegalArgumentException("Task cannot obejct does't has run method");
        }

        Object existingTask = beanTasks.putIfAbsent(name, task);
        if (!this.inDegree.containsKey(name)) {
            this.inDegree.put(name, 0);
        }

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
        if (!this.inDegree.containsKey(fromTask) || !this.inDegree.containsKey(toTask)) {
            throw new IllegalArgumentException("Empty task, plz add task first");
        }

        if (this.inDegree.containsKey(toTask)) {
            Integer val = this.inDegree.get(toTask);
            this.inDegree.put(toTask, val + 1);
        } else {
            this.inDegree.put(toTask, 1);
        }

        this.pointTo.computeIfAbsent(fromTask, k -> new ArrayList<>()).add(toTask);
    }

    /**
     * execute task concurrentlly by bfs topologicalsort.
     *
     * @return list task running status
     */
    public List<Pair<String, Boolean>> executeConcurrent() {

        if (!this.dependencyCheck) {
            throw new RuntimeException("check your dependecy before running");
        }

        Queue<String> queue = new LinkedList<String>();
        Map<String, Integer> sortIndegree = new HashMap<>(inDegree);

        for (Map.Entry<String, Integer> entry : sortIndegree.entrySet()) {
            if (entry.getValue() == 0) {
                queue.add(entry.getKey());
            }
        }
        List<Pair<String, Boolean>> excuteResults = new ArrayList<>();
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

            List<Pair<String, Boolean>> layerResults = doExcute(layerTask);
            excuteResults.addAll(layerResults);

            // handing resulsts
            if (this.dagRunningMode == RunningMode.INTERRUP && isInterrup(layerResults)) {
                break;
            }
        }
        return excuteResults;
    }

    /**
     * is interrup.
     *
     * @param layerResults list of task running status.
     * @return is interruped
     */
    private boolean isInterrup(List<Pair<String, Boolean>> layerResults) {
        for (Pair<String, Boolean> res : layerResults) {
            boolean taskSucced = res.getSecond();
            if (!taskSucced) {
                return true;
            }
        }
        return false;
    }

    /**
     * doExcute task concurrentlly.
     *
     * @param sequenceTask list of task obejct in sequence order.
     * @return list of excute results
     */
    private List<Pair<String, Boolean>> doExcute(List<String> sequenceTask) {
        List<CompletableFuture<Pair<String, Boolean>>> futureTask = new ArrayList<>();
        for (String taskName : sequenceTask) {
            LOGGER.info("cur running task: " + taskName);
            if (beanTasks.containsKey(taskName)) {
                Object taskObj = beanTasks.get(taskName);
                futureTask.add(CompletableFuture.supplyAsync(() -> {
                    try {
                        Class<?> taskObjClass = taskObj.getClass();
                        Method runMethod = taskObjClass.getMethod("run");
                        runMethod.invoke(taskObj);
                        return Pair.of(taskName, true);
                    } catch (Exception e) {
                        throw new CompletionException(e);
                    }
                }).exceptionally(e -> {
                    LOGGER.error("Task " + taskName + " failed with exception: ", e);
                    return Pair.of(taskName, false);
                }));
            }
        }

        List<Pair<String, Boolean>> excuteResults = new ArrayList<>();
        for (CompletableFuture<Pair<String, Boolean>> future : futureTask) {
            try {
                excuteResults.add(future.get());
            } catch (Exception e) {
                LOGGER.error("Error getting result from future: {}", e.getMessage());
            }
        }

        return excuteResults;
    }

    /**
     * check dependency before running.
     *
     */
    public void dependencyCheck() {
        if (numTask == 0) {
            throw new RuntimeException("No task in dag");
        }
        if (!canFinish()) {
            throw new IllegalArgumentException("Forbidden dag, check dependency");
        }
        this.dependencyCheck = true;
    }

    /**
     * whether cuurent dag graph is able to excute.
     *
     * @return whether task dag graph exsit circle.
     */
    private boolean canFinish() {
        Queue<String> queue = new LinkedList<String>();
        Map<String, Integer> sortIndegree = new HashMap<>(this.inDegree);

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
                List<String> toTasks = this.pointTo.get(task);
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
        return cnt == this.numTask;
    }

    /**
     * topologicalSort.
     *
     * @return task list in topological order.
     */
    private List<String> topologicalSort() {

        Queue<String> queue = new LinkedList<String>();
        Map<String, Integer> sortIndegree = new HashMap<>(inDegree);

        for (Map.Entry<String, Integer> entry : sortIndegree.entrySet()) {
            if (entry.getValue() == 0) {
                queue.add(entry.getKey());
            }
        }

        List<String> sequenceTask = new ArrayList<>();

        while (!queue.isEmpty()) {
            int curLayerSize = queue.size();

            while (curLayerSize > 0) {

                String task = queue.poll();
                sequenceTask.add(task);
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

        return sequenceTask;
    }
}
