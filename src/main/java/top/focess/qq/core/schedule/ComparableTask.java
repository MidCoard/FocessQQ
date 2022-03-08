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
        synchronized (this.task) {
            if (this.isCancelled)
                return false;
            if (this.task.isFinished())
                return false;
            if (mayInterruptIfRunning) {
                if (!this.task.isSingleThread())
                    throw new UnsupportedOperationException();
                if (this.task.isRunning())
                    this.task.cancel0();
            } else if (this.task.isRunning())
                return false;
            this.isCancelled = true;
            this.task.notifyAll();
            return !this.task.isRunning();
        }
    }

    public boolean isCancelled() {
        synchronized (this.task) {
            return this.isCancelled;
        }
    }

    long getTime(){
        return this.time;
    }

    ITask getTask() {
        return this.task;
    }
}
