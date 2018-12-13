package multithreading;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executor;

public class ThreadPool implements Executor {
    private final Queue<Runnable> workQueue = new ConcurrentLinkedQueue<>();
    private final List<Runnable> notRunTasks;
    private final Runnable lastTask;
    private final Object lock = new Object();
    private final int sumTask;
    private int failedTask = 0;
    private int completedTask = 0;
    private int interruptedTask = 0;
    private volatile boolean isRunning = true;
    public ThreadPool(int nThreads, Runnable callback, Runnable... tasks) {
        sumTask = tasks.length;
        notRunTasks = new ArrayList<>(Arrays.asList(tasks));
        lastTask = callback;
        for (int i = 0; i < nThreads; i++) {
            new Thread(new TaskWorker()).start();
        }
    }
    public int getFailedTask() {
        synchronized (lock) {
            return failedTask;
        }
    }
    public int getCompletedTask() {
        synchronized (lock) {
            return completedTask;
        }
    }
    public int getInterruptedTask() {
        synchronized (lock) {
            return interruptedTask;
        }
    }
    public void interrupt() {
        synchronized (lock) {
            interruptedTask = notRunTasks.size();
            notRunTasks.clear();
            runLastTask();
        }
    }
    public boolean isFinished() {
        synchronized (lock) {
            return failedTask + completedTask + interruptedTask == sumTask;
        }
    }
    protected void beforeExecute(Thread t, Runnable r) {
        if (r != lastTask) {
            synchronized (lock) {
                notRunTasks.remove(r);
            }
        }
    }
    protected void afterExecute(Runnable r, Throwable t) {
        if (r != lastTask) {
            synchronized (lock) {
                if (t != null) {
                    failedTask++;
                }
                else {
                    completedTask++;
                }
                runLastTask();
            }
        }
        if(r == lastTask) {
            ThreadPool.this.shutdown();
        }
    }
    private void runLastTask() {
        if (failedTask + completedTask + interruptedTask == sumTask) {
            execute(lastTask);
        }
    }
    @Override
    public void execute(Runnable command) {
        if (isRunning) {
            workQueue.offer(command);
        }
    }
    public void shutdown() {
        isRunning = false;
    }
    private final class TaskWorker implements Runnable {
        @Override
        public void run() {
            while (isRunning) {
                Runnable nextTask = workQueue.poll();
                if (nextTask != null) {
                    beforeExecute(Thread.currentThread(), nextTask);
                    Object var = null;
                    try {
                        nextTask.run();
                    }
                    catch (Throwable e) {
                        var = e;
                    }
                    finally {
                        afterExecute(nextTask, (Throwable) var);
                    }
                }
            }
        }
    }
}

