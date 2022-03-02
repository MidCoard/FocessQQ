package top.focess.qq.core.schedule;

import top.focess.qq.FocessQQ;

public class ThreadPoolSchedulerThread extends Thread{

    private final Object NOTIFY = new Object();

    private boolean isAvailable = true;
    private ITask task;
    private boolean shouldStop = false;

    public ThreadPoolSchedulerThread(String name) {
        super(name);
    }

    @Override
    public void run() {
        while (true) {
            synchronized (NOTIFY) {
                try {
                    if (isAvailable)
                        NOTIFY.wait();
                    if (shouldStop)
                        break;
                    this.task.run();
                    this.isAvailable = true;
                } catch (Exception e) {
                    FocessQQ.getLogger().thrLang("exception-thread-pool-scheduler-thread",e);
                }
            }
        }
    }

    public boolean isAvailable() {
        return isAvailable;
    }

    public void startTask(ITask task) {
        synchronized (NOTIFY) {
            this.isAvailable = false;
            this.task = task;
            NOTIFY.notify();
        }
    }

    public void close() {
        synchronized (NOTIFY) {
            this.shouldStop = true;
            NOTIFY.notify();
        }
    }
}
