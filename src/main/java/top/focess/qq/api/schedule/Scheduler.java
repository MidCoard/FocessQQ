package top.focess.qq.api.schedule;

import top.focess.qq.api.plugin.Plugin;

import java.time.Duration;
import java.util.concurrent.Callable;

/**
 * Used to schedule task
 */
public interface Scheduler {

    /**
     * Run a task now
     *
     * @param runnable the task
     * @return the warped task
     */
    default Task run(Runnable runnable) {
        return this.run(runnable, Duration.ZERO);
    }

    /**
     * Run a task later
     *
     * @param runnable the task
     * @param delay the delay
     * @return the warped task
     */
    Task run(Runnable runnable, Duration delay);

    /**
     * Run a task timer
     *
     * @param runnable the task
     * @param delay the delay
     * @param period the period
     * @return the warped task
     */
    Task runTimer(Runnable runnable, Duration delay, Duration period);

    /**
     * Submit a task now
     *
     * @param callable the task
     * @param <V> the return type
     * @return the warped task
     */
    default  <V> Callback<V> submit(Callable<V> callable) {
        return this.submit(callable, Duration.ZERO);
    }

    /**
     * Submit a task later
     *
     * @param callable the task
     * @param delay the delay
     * @param <V> the return type
     * @return the warped task
     */
    <V> Callback<V> submit(Callable<V> callable, Duration delay);

    /**
     * Cancel all the tasks
     */
    void cancelAll();

    /**
     * Get the name of the scheduler
     *
     * @return the name of the scheduler
     */
    String getName();

    /**
     * Get the plugin
     *
     * @return the plugin
     */
    Plugin getPlugin();

    /**
     * Close this scheduler
     */
    void close();

    /**
     * Indicate whether this scheduler is closed or not
     *
     * @return true if this scheduler is closed, false otherwise
     */
    boolean isClosed();
}
