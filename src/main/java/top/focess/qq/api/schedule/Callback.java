package top.focess.qq.api.schedule;

import org.jetbrains.annotations.NotNull;

import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/**
 * The warped task. You can use this to handle callable processing
 *
 * @param <V> the target value type
 */
public interface Callback<V> extends Task, Future<V> {

    /**
     * Call the target value
     *
     * @return the target value
     */
    V call();

    /**
     * Wait for this task finished and call the target value
     *
     * @return the target value
     */
    default V waitCall() {
        while (!isFinished());
        return call();
    }

    /**
     * Indicate whether this task is done or not
     *
     * @return true if this task is finished, false otherwise
     * @see #isFinished()
     */
    @Override
    default boolean isDone() {
        return this.isFinished();
    }

    /**
     * Wait for this task finished and call the target value
     *
     * @see #waitCall()
     * @return the target value
     */
    @Override
    default V get() {
        return this.waitCall();
    }

    /**
     * This method is not supported
     *
     * @param timeout not supported
     * @param unit not supported
     * @return not supported
     */
    @Override
    V get(long timeout, @NotNull TimeUnit unit);
}
