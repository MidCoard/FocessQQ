package top.focess.qq.api.schedule;

import org.jetbrains.annotations.NotNull;

/**
 * Thrown to indicate a scheduler is closed
 */
public class SchedulerClosedException extends IllegalStateException {

    /**
     * Constructs a SchedulerClosedException
     *
     * @param scheduler the closed scheduler
     */
    public SchedulerClosedException(@NotNull final Scheduler scheduler) {
        super("Scheduler " + scheduler.getName() + " is closed.");
    }
}
