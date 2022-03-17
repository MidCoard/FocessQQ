package top.focess.qq.api.schedule;

import top.focess.qq.api.plugin.Plugin;

import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;

/**
 * The warped task. You can use this to handle runnable processing
 */
public interface Task{

    /**
     * Cancel this task
     *
     * @param mayInterruptIfRunning must be false
     * @return true if it is cancelled, false it cannot be cancelled, or it is already cancelled
     */
    boolean cancel(boolean mayInterruptIfRunning);

    /**
     * Cancel this task
     *
     * @return true if it is cancelled, false it cannot be cancelled, or it is already cancelled
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

    /**
     * wait until this task is finished
     *
     *
     * @throws ExecutionException if there is any exception in the execution processing
     * @throws InterruptedException if the task is interrupted
     * @throws CancellationException if the task is cancelled
     */
    void join() throws ExecutionException, InterruptedException, CancellationException;

}
