package top.focess.qq.api.schedule;

/**
 * Thrown to indicate that the task is not found
 */
public class TaskNotFoundError extends Error {

    /**
     * Constructs a TaskNotFoundException
     *
     * @param task the task
     */
    public TaskNotFoundError(final Task task) {
        super("Task " + task.getName() + " is not found.");
    }
}
