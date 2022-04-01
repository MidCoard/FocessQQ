package top.focess.qq.api.command;

import org.jetbrains.annotations.NotNull;

/**
 * The result after executing a Command.
 */
public enum CommandResult {
    /**
     * It is accepted by the CommandExecutor
     */
    ALLOW(1),
    /**
     * It is not accepted by the CommandExecutor
     */
    REFUSE(2),
    /**
     * It is not accepted by the Command
     */
    COMMAND_REFUSED(4),
    /**
     * It indicates that print help information to the receiver
     */
    ARGS(8),
    /**
     * It includes all CommandResult
     */
    ALL(ALLOW, REFUSE, COMMAND_REFUSED, ARGS),
    /**
     * It includes all negative CommandResult
     */
    NEGATIVE(REFUSE, COMMAND_REFUSED, ARGS),
    /**
     * No signal
     */
    NONE(0);

    /**
     * Its internal value
     */
    private final int value;

    CommandResult(final CommandResult result, final CommandResult... results) {
        this(toInt(result, results));
    }

    CommandResult(final int value) {
        this.value = value;
    }

    private static int toInt(@NotNull final CommandResult result, @NotNull final CommandResult[] results) {
        int ret = result.getValue();
        for (final CommandResult r : results)
            ret |= r.getValue();
        return ret;
    }

    public int getValue() {
        return this.value;
    }
}
