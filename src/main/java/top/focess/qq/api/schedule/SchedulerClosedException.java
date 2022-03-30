package top.focess.qq.api.schedule;

/**
 * Thrown to indicate a scheduler is closed
 */
public class SchedulerClosedException extends IllegalStateException {

    /**
     * Constructs a SchedulerClosedException
     *
     * @param scheduler the closed scheduler
     */
    public SchedulerClosedException(final Scheduler scheduler) {
        super("Scheduler " + scheduler.getName() + " is closed.");
    }
}
