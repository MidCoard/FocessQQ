package top.focess.qq.core.schedule;

import com.google.common.collect.Queues;
import org.jetbrains.annotations.NotNull;
import top.focess.qq.FocessQQ;
import top.focess.qq.api.exceptions.SchedulerClosedException;
import top.focess.qq.api.plugin.Plugin;
import top.focess.qq.api.schedule.Callback;
import top.focess.qq.api.schedule.Task;

import java.time.Duration;
import java.util.Queue;
import java.util.concurrent.Callable;

public class FocessScheduler extends AScheduler {

    private final Queue<ComparableTask> tasks = Queues.newPriorityBlockingQueue();

    private boolean shouldStop = false;

    public FocessScheduler(@NotNull Plugin plugin) {
        super(plugin);
        new SchedulerThread(this.getName()).start();
    }

    @Override
    public Task run(Runnable runnable, Duration delay) {
        synchronized (tasks) {
            if (this.shouldStop)
                throw new SchedulerClosedException(this);
            FocessTask task = new FocessTask(runnable, this);
            tasks.add(new ComparableTask(System.currentTimeMillis() + delay.toMillis(), task));
            this.tasks.notify();
            return task;
        }
    }

    @Override
    public Task runTimer(Runnable runnable, Duration delay, Duration period) {
        synchronized (tasks) {
            if (this.shouldStop)
                throw new SchedulerClosedException(this);
            FocessTask task = new FocessTask(runnable, period, this);
            tasks.add(new ComparableTask(System.currentTimeMillis() + delay.toMillis(), task));
            this.tasks.notify();
            return task;
        }
    }

    @Override
    public <V> Callback<V> submit(Callable<V> callable, Duration delay) {
        synchronized (tasks) {
            if (this.shouldStop)
                throw new SchedulerClosedException(this);
            FocessCallback<V> callback = new FocessCallback<>(callable, this);
            tasks.add(new ComparableTask(System.currentTimeMillis() + delay.toMillis(), callback));
            this.tasks.notify();
            return callback;
        }
    }

    @Override
    public void cancelAll() {
        synchronized (tasks) {
            tasks.clear();
        }
    }

    @Override
    public String getName() {
        return this.getPlugin().getName() + "-FocessScheduler";
    }

    @Override
    public void close() {
        super.close();
        shouldStop = true;
        this.cancelAll();
    }

    @Override
    public boolean isClosed() {
        return shouldStop;
    }

    private class SchedulerThread extends Thread {

        public SchedulerThread(String name) {
            super(name);
        }

        @Override
        public void run() {
            while (true) {
                try {
                    synchronized (tasks) {
                        if (shouldStop)
                            break;
                        if (tasks.isEmpty())
                            tasks.wait();
                        ComparableTask task = tasks.peek();
                        if (task != null)
                            synchronized (task) {
                                if (task.isCancelled()) {
                                    tasks.poll();
                                    continue;
                                }
                                if (task.getTime() <= System.currentTimeMillis()) {
                                    tasks.poll();
                                    task.getTask().run();
                                    if (task.getTask().isPeriod()) {
                                        tasks.add(new ComparableTask(System.currentTimeMillis() + task.getTask().getPeriod().toMillis(), task.getTask()));
                                    }
                                }
                            }
                    }
                } catch (Exception e) {
                    FocessQQ.getLogger().thrLang("exception-focess-scheduler",e);
                }
            }
        }
    }


}
