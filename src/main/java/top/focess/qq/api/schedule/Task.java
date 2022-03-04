package top.focess.qq.api.schedule;

import top.focess.qq.api.plugin.Plugin;

/**
 * The warped task. You can use this to handle runnable processing
 */
public interface Task{

    /**
     * Cancel this task
     *
     * @param mayInterruptIfRunning must be false
     * @return true if it is cancelled, false otherwise
     */
    boolean cancel(boolean mayInterruptIfRunning);

    /**
     * Cancel this task
     *
     * @return true if it is cancelled, false otherwise
     * @see #cancel(boolean)
     */
    default boolean cancel() {
        return cancel(false);
    }

    /**
     * Indicate whether this task is running or not
     *
     * @return true if the task is running, false otherwise
     */
    boolean isRunning();

    /**
     * Get the plugin it belongs to
     *
     * @return the plugin it belongs to
     */
    Plugin getPlugin();

    /**
     * Get the scheduler it belongs to
     *
     * @return the scheduler it belongs to
     */
    Scheduler getScheduler();

    /**
     * Get the name of the task
     *
     * @return the name of the task
     */
    String getName();

    /**
     * Indicate whether this task is a period-task or not
     *
     * @return true if it is a period-task, false otherwise
     */
    boolean isPeriod();

    /**
     * Indicate whether this task is finished or not
     *
     * @return true if this task is finished, false otherwise
     */
    boolean isFinished();

    /**
     * Indicate whether this task is cancelled or not
     *
     * @return true if it is cancelled, false otherwise
     */
    boolean isCancelled();

}
