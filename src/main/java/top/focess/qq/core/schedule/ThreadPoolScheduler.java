package top.focess.qq.core.schedule;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Queues;
import org.jetbrains.annotations.Nullable;
import top.focess.qq.FocessQQ;
import top.focess.qq.api.plugin.Plugin;
import top.focess.qq.api.schedule.Callback;
import top.focess.qq.api.schedule.SchedulerClosedException;
import top.focess.qq.api.schedule.Task;
import top.focess.qq.api.schedule.TaskNotFoundException;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.UUID;
import java.util.concurrent.Callable;

public class ThreadPoolScheduler extends AScheduler {

    final Map<ITask, ThreadPoolSchedulerThread> taskThreadMap = Maps.newConcurrentMap();
    private final Queue<ComparableTask> tasks = Queues.newPriorityBlockingQueue();
    private final List<ThreadPoolSchedulerThread> threads = Lists.newArrayList();
    private final boolean immediate;
    private final String name;
    private volatile boolean shouldStop;
    private int currentThread;

    public ThreadPoolScheduler(final Plugin plugin, final int poolSize, final boolean isImmediate, final String name) {
        super(plugin);
        this.name = name;
        for (int i = 0; i < poolSize; i++)
            this.threads.add(new ThreadPoolSchedulerThread(this, this.getName() + "-" + i));
        new SchedulerThread(this.getName()).start();
        this.immediate = isImmediate;
    }

    public ThreadPoolScheduler(final Plugin plugin, final int poolSize) {
        this(plugin, poolSize, false, plugin.getName() + "-ThreadPoolScheduler-" + UUID.randomUUID().toString().substring(0, 8));
    }

    @Override
    public synchronized Task run(final Runnable runnable, final Duration delay) {
        if (this.shouldStop)
            throw new SchedulerClosedException(this);
        final FocessTask task = new FocessTask(runnable, this);
        this.tasks.add(new ComparableTask(System.currentTimeMillis() + delay.toMillis(), task));
        this.notify();
        return task;
    }

    @Override
    public synchronized Task runTimer(final Runnable runnable, final Duration delay, final Duration period) {
        if (this.shouldStop)
            throw new SchedulerClosedException(this);
        final FocessTask task = new FocessTask(runnable, period, this);
        this.tasks.add(new ComparableTask(System.currentTimeMillis() + delay.toMillis(), task));
        this.notify();
        return task;
    }

    @Override
    public synchronized <V> Callback<V> submit(final Callable<V> callable, final Duration delay) {
        if (this.shouldStop)
            throw new SchedulerClosedException(this);
        final FocessCallback<V> callback = new FocessCallback<>(callable, this);
        this.tasks.add(new ComparableTask(System.currentTimeMillis() + delay.toMillis(), callback));
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
        this.cancelAll();
        for (final ThreadPoolSchedulerThread thread : this.threads)
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
        this.cancelAll();
        for (final ThreadPoolSchedulerThread thread : this.threads)
            thread.closeNow();
        this.notify();
    }

    public void cancel(final ITask task) {
        if (this.taskThreadMap.containsKey(task)) {
            this.taskThreadMap.get(task).cancel();
            this.taskThreadMap.remove(task);
        } else throw new TaskNotFoundException(task);
    }

    public void recreate(final String name) {
        for (int i = 0; i < this.threads.size(); i++)
            if (this.threads.get(i).getName().equals(name)) {
                this.threads.set(i, new ThreadPoolSchedulerThread(this, name));
                break;
            }
    }

    public void rerun(final ITask task) {
        if (this.shouldStop)
            return;
        this.tasks.add(new ComparableTask(System.currentTimeMillis() + task.getPeriod().toMillis(), task));
    }

    @Override
    public String toString() {
        return this.getName();
    }

    private class SchedulerThread extends Thread {

        public SchedulerThread(final String name) {
            super(name);
            this.setUncaughtExceptionHandler((t, e) -> {
                ThreadPoolScheduler.this.close();
                FocessQQ.getLogger().thrLang("exception-thread-pool-scheduler-uncaught", e, ThreadPoolScheduler.this.getName());
            });
        }

        @Nullable
        private ThreadPoolSchedulerThread getAvailableThread() {
            for (int i = 1; i <= ThreadPoolScheduler.this.threads.size(); i++) {
                final int next = (ThreadPoolScheduler.this.currentThread + i) % ThreadPoolScheduler.this.threads.size();
                if (ThreadPoolScheduler.this.threads.get(next).isAvailable()) {
                    ThreadPoolScheduler.this.currentThread = next;
                    return ThreadPoolScheduler.this.threads.get(next);
                }
            }
            if (ThreadPoolScheduler.this.immediate) {
                final ThreadPoolSchedulerThread thread = new ThreadPoolSchedulerThread(ThreadPoolScheduler.this, ThreadPoolScheduler.this.getName() + "-" + ThreadPoolScheduler.this.threads.size());
                ThreadPoolScheduler.this.threads.add(thread);
                return thread;
            }
            return null;
        }

        @Override
        public void run() {
            while (true) {
                try {
                    synchronized (ThreadPoolScheduler.this) {
                        if (ThreadPoolScheduler.this.shouldStop)
                            break;
                        if (ThreadPoolScheduler.this.tasks.isEmpty())
                            ThreadPoolScheduler.this.wait();
                    }
                    final ComparableTask task = ThreadPoolScheduler.this.tasks.peek();
                    if (task != null)
                        synchronized (task.getTask()) {
                            if (task.isCancelled()) {
                                ThreadPoolScheduler.this.tasks.poll();
                                continue;
                            }
                            if (task.getTime() <= System.currentTimeMillis()) {
                                final ThreadPoolSchedulerThread thread = this.getAvailableThread();
                                if (thread == null)
                                    continue;
                                ThreadPoolScheduler.this.tasks.poll();
                                ThreadPoolScheduler.this.taskThreadMap.put(task.getTask(), thread);
                                thread.startTask(task.getTask());
                            }
                        }
                } catch (final Exception e) {
                    FocessQQ.getLogger().thrLang("exception-thread-pool-scheduler", e);
                }
            }
        }
    }
}
