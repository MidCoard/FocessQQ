package top.focess.qq.core.schedule;

import top.focess.qq.api.schedule.Task;

import java.time.Duration;

interface ITask extends Task {

    boolean isSingleThread();

    void run();

    Duration getPeriod();

    void setNativeTask(ComparableTask task);

    default void cancel0() {
        if (this.getScheduler() instanceof ThreadPoolScheduler)
            ((ThreadPoolScheduler) this.getScheduler()).cancel(this);
        else throw new UnsupportedOperationException();
        this.forceCancel();
    }

    void forceCancel();
}
