package top.focess.qq.api.command;

/**
 * Thrown to indicate there is an existed command named this name
 */
public class CommandDuplicateException extends IllegalStateException {
    /**
     * Constructs a CommandDuplicateException
     * @param name the name of the duplicated command
     */
    public CommandDuplicateException(final String name) {
        super("Command " + name + " is duplicated.");
    }
}
