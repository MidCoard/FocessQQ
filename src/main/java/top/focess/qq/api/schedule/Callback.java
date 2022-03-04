package top.focess.qq.api.schedule;

import org.jetbrains.annotations.NotNull;

import java.util.concurrent.*;

/**
 * The warped task. You can use this to handle callable processing
 *
 * @param <V> the target value type
 */
public interface Callback<V> extends Task, Future<V> {

    /**
     * Call the target value
     * @throws CancellationException if the task is cancelled
     * @throws top.focess.qq.api.exceptions.TaskNotFinishedException if the task is not finished
     * @return the target value
     */
    V call();

    /**
     * Wait for this task finished and call the target value
     * @throws InterruptedException if the current thread was interrupted while waiting
     * @throws ExecutionException if there is any exception in the execution processing
     * @return the target value
     */
    default V waitCall() throws InterruptedException, ExecutionException {
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
     * @throws InterruptedException if the current thread was interrupted while waiting
     * @throws ExecutionException if there is any exception in the execution processing
     * @return the target value
     */
    @Override
    default  V get() throws InterruptedException, ExecutionException {
        return this.waitCall();
    }

    /**
     * Wait for the time and call the target value
     *
     * @param timeout the timeout
     * @param unit the time unit
     * @throws InterruptedException if the current thread was interrupted while waiting
     * @throws ExecutionException if there is any exception in the execution processing
     * @throws TimeoutException if the time is out
     * @throws CancellationException if the task is cancelled
     * @return the target value
     */
    @Override
    V get(long timeout,@NotNull TimeUnit unit)
            throws InterruptedException, ExecutionException, TimeoutException, CancellationException;
}
