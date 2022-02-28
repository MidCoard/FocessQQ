package top.focess.qq.api.exceptions;

/**
 * Thrown to indicate there is an existed command named this name
 */
public class CommandDuplicateException extends RuntimeException {
    /**
     * Constructs a CommandDuplicateException
     * @param name the name of the duplicated command
     */
    public CommandDuplicateException(String name) {
        super("Command " + name + " is duplicated.");
    }
}
