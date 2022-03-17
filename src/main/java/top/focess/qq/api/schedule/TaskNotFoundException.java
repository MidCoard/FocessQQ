package top.focess.qq.api.schedule;

import top.focess.qq.api.schedule.Task;

/**
 * Thrown to indicate that the task is not found
 */
public class TaskNotFoundException extends IllegalStateException {

    /**
     * Constructs a TaskNotFoundException
     * @param task the task
     */
    public TaskNotFoundException(Task task) {
        super("Task " + task.getName() + " is not found.");
    }
}
