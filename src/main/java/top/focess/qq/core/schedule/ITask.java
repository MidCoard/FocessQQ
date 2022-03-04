package top.focess.qq.core.schedule;

import top.focess.qq.api.schedule.Task;

import java.time.Duration;
import java.util.concurrent.ExecutionException;

interface ITask extends Task {

    boolean isSingleThread();

    void run() throws ExecutionException;

    Duration getPeriod();

    void setNativeTask(ComparableTask task);

    default void cancel0() {
        if (this.getScheduler() instanceof ThreadPoolScheduler)
            ((ThreadPoolScheduler) this.getScheduler()).cancel(this);
        else throw new UnsupportedOperationException();
        this.clear();
    }

    void clear();

    void startRun();

    void endRun();

    void setException(ExecutionException e);
}
