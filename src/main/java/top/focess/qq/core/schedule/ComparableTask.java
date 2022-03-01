package top.focess.qq.core.schedule;

import org.jetbrains.annotations.NotNull;

public class ComparableTask implements Comparable<ComparableTask> {

    private final long time;
    private final ITask task;
    private boolean isCancelled = false;

    public ComparableTask(long time, ITask task) {
        this.time = time;
        this.task = task;
        this.task.setNativeTask(this);
    }

    @Override
    public int compareTo(@NotNull ComparableTask o) {
        return Long.compare(this.time, o.time);
    }

    public boolean cancel(boolean mayInterruptIfRunning) {
        if (mayInterruptIfRunning)
            throw new UnsupportedOperationException();
        this.isCancelled = true;
        return this.task.isPeriod() || !this.task.isRunning();
    }

    public boolean isCancelled() {
        return this.isCancelled;
    }

    long getTime(){
        return this.time;
    }

    ITask getTask() {
        return this.task;
    }
}
