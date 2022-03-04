package top.focess.qq.core.schedule;

import com.google.common.collect.Maps;
import com.google.common.collect.Queues;
import top.focess.qq.FocessQQ;
import top.focess.qq.api.exceptions.SchedulerClosedException;
import top.focess.qq.api.exceptions.TaskNotFoundException;
import top.focess.qq.api.plugin.Plugin;
import top.focess.qq.api.schedule.Callback;
import top.focess.qq.api.schedule.Task;

import java.time.Duration;
import java.util.Map;
import java.util.Queue;
import java.util.UUID;
import java.util.concurrent.Callable;

public class ThreadPoolScheduler extends AScheduler {

    private final Queue<ComparableTask> tasks = Queues.newPriorityBlockingQueue();

    private final Map<ITask,ThreadPoolSchedulerThread> taskThreadMap = Maps.newHashMap();

    private final ThreadPoolSchedulerThread[] threads;

    private boolean shouldStop = false;

    private int currentThread = 0;

    private final String name;

    public ThreadPoolScheduler(Plugin plugin, int poolSize) {
        super(plugin);
        this.name = this.getPlugin().getName() + "-ThreadPoolScheduler-" + UUID.randomUUID().toString().substring(0,8);
        this.threads = new ThreadPoolSchedulerThread[poolSize];
        for (int i = 0; i < poolSize; i++)
            threads[i] = new ThreadPoolSchedulerThread(this,this.getName() + "-" + i);
        new SchedulerThread(this.getName()).start();
    }

    @Override
    public synchronized Task run(Runnable runnable, Duration delay) {
        if (this.shouldStop)
            throw new SchedulerClosedException(this);
        FocessTask task = new FocessTask(runnable, this);
        tasks.add(new ComparableTask(System.currentTimeMillis() + delay.toMillis(), task));
        this.notify();
        return task;
    }

    @Override
    public synchronized Task runTimer(Runnable runnable, Duration delay, Duration period) {
        if (this.shouldStop)
            throw new SchedulerClosedException(this);
        FocessTask task = new FocessTask(runnable, period, this);
        tasks.add(new ComparableTask(System.currentTimeMillis() + delay.toMillis(), task));
        this.notify();
        return task;
    }

    @Override
    public synchronized <V> Callback<V> submit(Callable<V> callable, Duration delay) {
        if (this.shouldStop)
            throw new SchedulerClosedException(this);
        FocessCallback<V> callback = new FocessCallback<>(callable, this);
        tasks.add(new ComparableTask(System.currentTimeMillis() + delay.toMillis(), callback));
        this.notify();
        return callback;
    }

    @Override
    public void cancelAll() {
        this.tasks.clear();
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public synchronized void close() {
        super.close();
        this.shouldStop = true;
        cancelAll();
        for (ThreadPoolSchedulerThread thread : this.threads)
            thread.close();
        this.notify();
    }

    @Override
    public boolean isClosed() {
        return this.shouldStop;
    }

    public void cancel(ITask task) {
        if (taskThreadMap.containsKey(task)) {
            taskThreadMap.get(task).cancel();
            taskThreadMap.remove(task);
        }
        else throw new TaskNotFoundException(task);
    }

    public void recreate(String name) {
        for (int i = 0;i<threads.length;i++)
            if (threads[i].getName().equals(name)) {
                threads[i] = new ThreadPoolSchedulerThread(this, name);
                break;
            }
    }

    public void rerun(ITask task) {
        tasks.add(new ComparableTask(System.currentTimeMillis() + task.getPeriod().toMillis(), task));
    }

    private class SchedulerThread extends Thread {

        public SchedulerThread(String name) {
            super(name);
        }

        private ThreadPoolSchedulerThread getAvailableThread() {
            for (int i = 1;i <= threads.length;i++) {
                int next = (currentThread + i) % threads.length;
                if (threads[next].isAvailable()) {
                    currentThread = next;
                    return threads[next];
                }
            }
            return null;
        }

        @Override
        public void run() {
            while (true) {
                try {
                    if (shouldStop)
                        break;
                    if (tasks.isEmpty())
                        synchronized (ThreadPoolScheduler.this) {
                            ThreadPoolScheduler.this.wait();
                        }
                    ComparableTask task = tasks.peek();
                    if (task != null)
                        synchronized (task.getTask()) {
                            if (task.isCancelled()) {
                                tasks.poll();
                                continue;
                            }
                            if (task.getTime() <= System.currentTimeMillis()) {
                                ThreadPoolSchedulerThread thread = getAvailableThread();
                                if (thread == null)
                                    continue;
                                tasks.poll();
                                taskThreadMap.put(task.getTask(), thread);
                                thread.startTask(task.getTask());
                            }
                        }
                    sleep(0);
                } catch (Exception e) {
                    FocessQQ.getLogger().thrLang("exception-thread-pool-scheduler",e);
                }
            }
        }
    }

    @Override
    public String toString() {
        return this.getName();
    }
}
