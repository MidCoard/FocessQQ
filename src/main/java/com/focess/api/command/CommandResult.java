package com.focess.api.command;

/**
 * The result after executing a Command.
 */
public enum CommandResult {
    /**
     * It is accepted by the CommandExecutor
     */
    ALLOW(1),
    /**
     * It is not accepted by the CommandExecutor or the Command
     */
    REFUSE(2),
    MESSAGE(4),
    /**
     * It indicates that print help information to the receiver
     */
    ARGS(8),
    /**
     * It includes all CommandResult
     */
    ALL(ALLOW, REFUSE, MESSAGE, ARGS),
    /**
     * It includes all positive CommandResult
     */
    POSITIVE(ALLOW, MESSAGE),
    /**
     * No signal
     */
    NONE(0);

    /**
     * Its internal value
     */
    private final int value;

    CommandResult(CommandResult result, CommandResult... results) {
        this(toInt(result, results));
    }

    CommandResult(int value) {
        this.value = value;
    }

    private static int toInt(CommandResult result, CommandResult[] results) {
        int ret = result.getValue();
        for (CommandResult r : results)
            ret |= r.getValue();
        return ret;
    }

    public int getValue() {
        return value;
    }
}
