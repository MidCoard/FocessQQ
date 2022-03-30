package top.focess.qq.api.schedule;

/**
 * Thrown to indicate the task is not finished
 */
public class TaskNotFinishedException extends IllegalStateException {

    /**
     * Constructs a TaskNotFinishedException
     *
     * @param callback the task
     * @param <V>      the task return type
     */
    public <V> TaskNotFinishedException(final Callback<V> callback) {
        super("Task " + callback.getName() + " is not finished.");
    }
}
