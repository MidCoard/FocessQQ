package top.focess.qq.api.command;

/**
 * Represents a CommandResult executor to define how to execute the CommandResult after executing a special CommandExecutor.
 *
 * This is a functional interface whose functional method is {@link CommandResultExecutor#execute(CommandResult)}.
 */
@FunctionalInterface
public interface CommandResultExecutor {

    /**
     * Used to have response to CommandResult after executing a special CommandExecutor
     *
     * @param commandResult the CommandResult after executing a special CommandExecutor
     */
    void execute(CommandResult commandResult);
}
