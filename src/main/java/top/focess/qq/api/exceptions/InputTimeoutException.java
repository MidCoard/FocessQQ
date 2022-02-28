package top.focess.qq.api.exceptions;

import top.focess.qq.api.util.IOHandler;

/**
 * Thrown to indicate {@link IOHandler} has waited for more than 10 minutes to get input String
 */
public class InputTimeoutException extends RuntimeException {
    /**
     * Constructs a InputTimeoutException
     */
    public InputTimeoutException() {
        super("IOHandler has waited for more than 10 minutes to get input string.");
    }
}
