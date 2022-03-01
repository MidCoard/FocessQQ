package top.focess.qq.api.schedule;

import top.focess.qq.api.plugin.Plugin;

public interface Task{

    boolean cancel(boolean mayInterruptIfRunning);

    default boolean cancel() {
        return cancel(false);
    }

    boolean isRunning();

    Plugin getPlugin();

    Scheduler getScheduler();

    String getName();

    boolean isPeriod();

    boolean isFinished();

    boolean isCancelled();
}
