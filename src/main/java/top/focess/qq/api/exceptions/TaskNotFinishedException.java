package top.focess.qq.api.exceptions;

import top.focess.qq.api.schedule.Callback;

/**
 * Thrown to indicate the task is not finished
 */
public class TaskNotFinishedException extends RuntimeException {

    /**
     * Constructs a TaskNotFinishedException
     *
     * @param callback the task
     * @param <V> the task return type
     */
    public <V> TaskNotFinishedException(Callback<V> callback) {
        super("Task " + callback.getName() + " is not finished.");
    }
}
