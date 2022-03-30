package top.focess.qq.core.schedule;

import org.jetbrains.annotations.Nullable;
import top.focess.qq.FocessQQ;

import java.util.concurrent.ExecutionException;

public class ThreadPoolSchedulerThread extends Thread{

    private final Object lock = new Object();
    private final ThreadPoolScheduler scheduler;
    private final String name;

    private boolean isAvailable = true;
    @Nullable
    private ITask task;
    private boolean shouldStop;

    public ThreadPoolSchedulerThread(final ThreadPoolScheduler scheduler, final String name) {
        super(name);
        this.scheduler = scheduler;
        this.name = name;
        this.setUncaughtExceptionHandler((t, e) -> {
            this.shouldStop = true;
            this.isAvailable = false;
            if (this.task != null) {
                this.task.setException(new ExecutionException(e));
                this.task.endRun();
                scheduler.taskThreadMap.remove(this.task);
            }
            FocessQQ.getLogger().thrLang("exception-thread-pool-scheduler-thread-uncaught",e,t.getName());
            scheduler.recreate(this.name);
        });
        this.start();
    }

    @Override
    public void run() {
        while (true) {
            try {
                if (this.isAvailable)
                    synchronized (this.lock) {
                        this.lock.wait();
                    }
                if (this.shouldStop)
                    break;
                if (this.task != null) {
                    this.task.startRun();
                    try {
                        this.task.run();
                    } catch (final Exception e) {
                        this.task.setException(new ExecutionException(e));
                    }
                    this.task.endRun();
                    this.scheduler.taskThreadMap.remove(this.task);
                    if (this.task.isPeriod())
                        this.scheduler.rerun(this.task);
                    this.task = null;
                }
                this.isAvailable = true;
            } catch (final Exception e) {
                FocessQQ.getLogger().thrLang("exception-thread-pool-scheduler-thread",e);
            }
        }
    }

    public boolean isAvailable() {
        return this.isAvailable;
    }

    public void startTask(final ITask task) {
        this.isAvailable = false;
        this.task = task;
        synchronized (this.lock) {
            this.lock.notify();
        }
    }

    public void close() {
        this.shouldStop = true;
        synchronized (this.lock) {
            this.lock.notify();
        }
    }

    public void closeNow() {
        this.close();
        this.stop();
    }

    public void cancel() {
        this.stop();
        this.scheduler.recreate(this.name);
    }

}
