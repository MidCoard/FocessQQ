package top.focess.qq.core.schedule;

import top.focess.qq.FocessQQ;

public class ThreadPoolSchedulerThread extends Thread{

    private final Object lock = new Object();
    private final ThreadPoolScheduler scheduler;
    private final String name;

    private boolean isAvailable = true;
    private ITask task;
    private boolean shouldStop = false;

    public ThreadPoolSchedulerThread(ThreadPoolScheduler scheduler, String name) {
        super(name);
        this.scheduler = scheduler;
        this.name = name;
        this.start();
    }

    @Override
    public void run() {
        while (true) {
            try {
                if (isAvailable)
                    synchronized (lock) {
                        lock.wait();
                    }
                if (shouldStop)
                    break;
                if (this.task != null) {
                    this.task.run();
                    this.task = null;
                }
                this.isAvailable = true;
            } catch (Exception e) {
                FocessQQ.getLogger().thrLang("exception-thread-pool-scheduler-thread",e);
            }
        }
    }

    public boolean isAvailable() {
        return isAvailable;
    }

    public void startTask(ITask task) {
        this.isAvailable = false;
        this.task = task;
        synchronized (lock) {
            lock.notify();
        }
    }

    public void close() {
        this.shouldStop = true;
        synchronized (lock) {
            lock.notify();
        }
    }

    public void cancel() {
        this.stop();
        this.scheduler.recreate(this.name);
    }

}
