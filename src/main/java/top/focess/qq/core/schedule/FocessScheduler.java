package top.focess.qq.core.schedule;

import com.google.common.collect.Queues;
import org.jetbrains.annotations.NotNull;
import top.focess.qq.FocessQQ;
import top.focess.qq.api.schedule.SchedulerClosedException;
import top.focess.qq.api.plugin.Plugin;
import top.focess.qq.api.schedule.Callback;
import top.focess.qq.api.schedule.Task;

import java.time.Duration;
import java.util.Queue;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;

public class FocessScheduler extends AScheduler {

    private final Queue<ComparableTask> tasks = Queues.newPriorityBlockingQueue();
    private final String name;
    private final Thread thread;

    private volatile boolean shouldStop = false;

    public FocessScheduler(@NotNull Plugin plugin, String name) {
        super(plugin);
        this.name = name;
        this.thread = new SchedulerThread(this.getName());
        this.thread.start();
    }

    public FocessScheduler(@NotNull Plugin plugin) {
        this(plugin, plugin.getName() + "-FocessScheduler-" + UUID.randomUUID().toString().substring(0,8));
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
        tasks.clear();
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public synchronized void close() {
        super.close();
        shouldStop = true;
        this.cancelAll();
        this.notify();
    }

    @Override
    public boolean isClosed() {
        return shouldStop;
    }

    @Override
    public synchronized void closeNow() {
        this.close();
        this.thread.stop();
    }

    private class SchedulerThread extends Thread {

        public SchedulerThread(String name) {
            super(name);
        }

        @Override
        public void run() {
            while (true) {
                try {
                    synchronized (FocessScheduler.this) {
                        if (shouldStop)
                            break;
                        if (tasks.isEmpty())
                            FocessScheduler.this.wait();
                    }
                    ComparableTask task = tasks.peek();
                    if (task != null) {
                        synchronized (task.getTask()) {
                            if (task.isCancelled()) {
                                tasks.poll();
                                continue;
                            }
                            if (task.getTime() <= System.currentTimeMillis()) {
                                tasks.poll();
                                task.getTask().startRun();
                            }
                        }
                        if (task.getTask().isRunning()) {
                            try {
                                task.getTask().run();
                            } catch(Exception e) {
                                task.getTask().setException(new ExecutionException(e));
                            }
                            task.getTask().endRun();
                            if (task.getTask().isPeriod())
                                tasks.add(new ComparableTask(System.currentTimeMillis() + task.getTask().getPeriod().toMillis(), task.getTask()));
                        }
                    }
                    sleep(0);
                } catch (Exception e) {
                    FocessQQ.getLogger().thrLang("exception-focess-scheduler",e);
                }
            }
        }
    }


    @Override
    public String toString() {
        return this.getName();
    }
}
