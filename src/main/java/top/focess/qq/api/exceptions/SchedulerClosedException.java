package top.focess.qq.api.exceptions;

import top.focess.qq.api.schedule.Scheduler;

/**
 * Thrown to indicate a scheduler is closed
 */
public class SchedulerClosedException extends RuntimeException{

    /**
     * Constructs a SchedulerClosedException
     *
     * @param scheduler the closed scheduler
     */
    public SchedulerClosedException(Scheduler scheduler) {
        super("Scheduler " + scheduler.getName() + " is closed.");
    }
}
