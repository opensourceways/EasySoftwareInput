package com.easysoftwareinput.easysoftwareinput;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.util.Pair;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

import com.easysoftwareinput.common.dag.DagTaskExecutor;
import com.easysoftwareinput.common.dag.DagTaskExecutor.RunningMode;
import com.easysoftwareinput.easysoftwareinput.TestTask;

@SpringBootTest
public class DagTaskExecutorTest {

    /**
     * 测试: 循环依赖错误
     */
    @Test
    public void testCircularDependencyAddWithError() {

        TestTask testTaskA = new TestTask();
        TestTask testTaskB = new TestTask();

        DagTaskExecutor dag = new DagTaskExecutor();

        dag.addTaskObj("TaskA", testTaskA);
        dag.addTaskObj("TaskB", testTaskB);
        // 循环依赖C
        dag.addDependency("TaskA", "TaskB");
        dag.addDependency("TaskB", "TaskA");
        // 异常种类检查
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> dag.dependencyCheck());
        // 异常消息检查
        assertEquals("Forbidden dag, check dependency", exception.getMessage());
    }

    /**
     * 测试: 环路错误
     */
    @Test
    public void testLoopDependencyAddWithError() {

        TestTask testTaskA = new TestTask();
        TestTask testTaskB = new TestTask();
        TestTask testTaskC = new TestTask();
        TestTask testTaskD = new TestTask();

        DagTaskExecutor dag = new DagTaskExecutor();

        dag.addTaskObj("TaskA", testTaskA);
        dag.addTaskObj("TaskB", testTaskB);
        dag.addTaskObj("TaskC", testTaskC);
        dag.addTaskObj("TaskD", testTaskD);
        // Loop依赖
        dag.addDependency("TaskA", "TaskB");
        dag.addDependency("TaskB", "TaskC");
        dag.addDependency("TaskC", "TaskD");
        dag.addDependency("TaskD", "TaskA");
        // 异常种类检查
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> dag.dependencyCheck());
        // 异常消息检查
        assertEquals("Forbidden dag, check dependency", exception.getMessage());
    }

    /**
     * 测试: task未实现run方法错误
     */
    @Test
    public void testTaskAddWithError() {
        TestTask testTaskA = new TestTask();
        Object erroTask = new Object();

        DagTaskExecutor dag = new DagTaskExecutor();

        dag.addTaskObj("TaskA", testTaskA);
        // 异常种类检查
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> dag.addTaskObj("TaskB", erroTask));
        // 异常消息检查
        assertEquals("Task cannot obejct does't has run method", exception.getMessage());
    }

    /**
     * 测试: 运行错误
     */
    @Test
    public void testExecuteConcurrentWithError() {

        TestTask testTaskA = new TestTask();
        TestTask testTaskB = new TestTask();
        TestTask testTaskC = new TestTask();
        TestTask testTaskD = new TestTask();

        DagTaskExecutor dag = new DagTaskExecutor();

        dag.addTaskObj("TaskA", testTaskA);
        dag.addTaskObj("TaskB", testTaskB);
        dag.addTaskObj("TaskC", testTaskC);
        dag.addTaskObj("TaskD", testTaskD);
        // Loop依赖
        dag.addDependency("TaskA", "TaskB");
        dag.addDependency("TaskB", "TaskC");
        dag.addDependency("TaskC", "TaskD");
        dag.addDependency("TaskD", "TaskA");
        // 异常种类检查
        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> dag.executeConcurrent());
        // 异常消息检查
        assertEquals("check your dependecy before running", exception.getMessage());
    }

    /**
     * 测试: 任务运行时错误-interuppt模式检查
     */
    @Test
    public void testInterupptRunningTaskWithError() {

        TestTask testTaskA = new TestTask();
        exceptionTestTask testTaskB = new exceptionTestTask();

        DagTaskExecutor dag = new DagTaskExecutor();

        dag.addTaskObj("TaskA", testTaskA);
        dag.addTaskObj("errorTaskB", testTaskB);

        dag.addDependency("errorTaskB", "TaskA");

        dag.dependencyCheck();

        List<Pair<String, Boolean>> runningResult = dag.executeConcurrent();

        // 中断模式检查，返回一个任务结果
        assertEquals(1, runningResult.size());

        // 执行顺序 & 结果检查
        Pair<String, Boolean> firstTask = runningResult.get(0);
        assertEquals("errorTaskB", firstTask.getFirst());
        assertFalse(firstTask.getSecond());

    }

    /**
     * 测试: 任务运行时错误-noninteruppt模式检查
     */
    @Test
    public void testNonInterupptRunningTaskWithError() {

        TestTask testTaskA = new TestTask();
        exceptionTestTask testTaskB = new exceptionTestTask();

        DagTaskExecutor dag = new DagTaskExecutor(RunningMode.NON_INTERRUP);

        dag.addTaskObj("TaskA", testTaskA);
        dag.addTaskObj("errorTaskB", testTaskB);

        dag.addDependency("errorTaskB", "TaskA");

        dag.dependencyCheck();

        List<Pair<String, Boolean>> runningResult = dag.executeConcurrent();

        // 中断模式检查，返回两个任务结果
        assertEquals(2, runningResult.size());

        // 执行顺序 & 结果检查
        Pair<String, Boolean> firstTask = runningResult.get(0);
        assertEquals("errorTaskB", firstTask.getFirst());
        assertFalse(firstTask.getSecond());

        // 执行顺序 & 结果检查
        Pair<String, Boolean> sencondTask = runningResult.get(1);
        assertEquals("TaskA", sencondTask.getFirst());
        assertTrue(sencondTask.getSecond());

    }

    /**
     * 测试: 任务执行顺序检查
     */
    @Test
    public void testRunningTask() {

        TestTask testTaskA = new TestTask();
        TestTask testTaskB = new TestTask();
        TestTask testTaskC = new TestTask();
        TestTask testTaskD = new TestTask();

        DagTaskExecutor dag = new DagTaskExecutor();

        dag.addTaskObj("TaskA", testTaskA);
        dag.addTaskObj("TaskB", testTaskB);
        dag.addTaskObj("TaskC", testTaskC);
        dag.addTaskObj("TaskD", testTaskD);

        dag.addDependency("TaskA", "TaskB");
        dag.addDependency("TaskA", "TaskC");
        dag.addDependency("TaskB", "TaskD");
        dag.addDependency("TaskC", "TaskD");

        dag.dependencyCheck();

        List<Pair<String, Boolean>> runningResult = dag.executeConcurrent();
        // 返回4个任务结果
        assertEquals(4, runningResult.size());

        // 执行顺序 & 结果检查
        Pair<String, Boolean> lastTask = runningResult.get(3);
        assertEquals("TaskD", lastTask.getFirst());
        assertTrue(lastTask.getSecond());
    }

    /**
     * 测试: 空task执行错误检查
     */
    @Test
    public void testEmptyTaskWithError() {

        DagTaskExecutor dag = new DagTaskExecutor();

        // 异常种类检查
        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> dag.dependencyCheck());

        // 异常消息检查
        assertEquals("No task in dag", exception.getMessage());
    }
}
