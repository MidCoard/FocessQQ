package top.focess.qq.core.schedule;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Queues;
import org.jetbrains.annotations.Nullable;
import top.focess.qq.FocessQQ;
import top.focess.qq.api.schedule.SchedulerClosedException;
import top.focess.qq.api.schedule.TaskNotFoundException;
import top.focess.qq.api.plugin.Plugin;
import top.focess.qq.api.schedule.Callback;
import top.focess.qq.api.schedule.Task;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.UUID;
import java.util.concurrent.Callable;

public class ThreadPoolScheduler extends AScheduler {

    private final Queue<ComparableTask> tasks = Queues.newPriorityBlockingQueue();

    final Map<ITask,ThreadPoolSchedulerThread> taskThreadMap = Maps.newConcurrentMap();

    private final List<ThreadPoolSchedulerThread> threads = Lists.newArrayList();
    private final boolean immediate;

    private volatile boolean shouldStop = false;

    private int currentThread = 0;

    private final String name;

    public ThreadPoolScheduler(Plugin plugin, int poolSize, boolean isImmediate,String name) {
        super(plugin);
        this.name = name;
        for (int i = 0; i < poolSize; i++)
            threads.add(new ThreadPoolSchedulerThread(this,this.getName() + "-" + i));
        new SchedulerThread(this.getName()).start();
        this.immediate = isImmediate;
    }

    public ThreadPoolScheduler(Plugin plugin, int poolSize) {
        this(plugin, poolSize, false,plugin.getName() + "-ThreadPoolScheduler-" + UUID.randomUUID().toString().substring(0,8));
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

    @Override
    public void closeNow() {
        super.close();
        this.shouldStop = true;
        cancelAll();
        for (ThreadPoolSchedulerThread thread : this.threads)
            thread.closeNow();
        this.notify();
    }

    public void cancel(ITask task) {
        if (taskThreadMap.containsKey(task)) {
            taskThreadMap.get(task).cancel();
            taskThreadMap.remove(task);
        }
        else throw new TaskNotFoundException(task);
    }

    public void recreate(String name) {
        for (int i = 0;i<threads.size();i++)
            if (threads.get(i).getName().equals(name)) {
                threads.set(i, new ThreadPoolSchedulerThread(this, name));
                break;
            }
    }

    public void rerun(ITask task) {
        if (this.shouldStop)
            return;
        tasks.add(new ComparableTask(System.currentTimeMillis() + task.getPeriod().toMillis(), task));
    }

    private class SchedulerThread extends Thread {

        public SchedulerThread(String name) {
            super(name);
            this.setUncaughtExceptionHandler((t,e)->{
                close();
                FocessQQ.getLogger().thrLang("exception-thread-pool-scheduler-uncaught",e,ThreadPoolScheduler.this.getName());
            });
        }

        @Nullable
        private ThreadPoolSchedulerThread getAvailableThread() {
            for (int i = 1;i <= threads.size();i++) {
                int next = (currentThread + i) % threads.size();
                if (threads.get(next).isAvailable()) {
                    currentThread = next;
                    return threads.get(next);
                }
            }
            if (immediate) {
                ThreadPoolSchedulerThread thread = new ThreadPoolSchedulerThread(ThreadPoolScheduler.this, ThreadPoolScheduler.this.getName() + "-" + threads.size());
                threads.add(thread);
                return thread;
            }
            return null;
        }

        @Override
        public void run() {
            while (true) {
                try {
                    synchronized (ThreadPoolScheduler.this) {
                        if (shouldStop)
                            break;
                        if (tasks.isEmpty())
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
