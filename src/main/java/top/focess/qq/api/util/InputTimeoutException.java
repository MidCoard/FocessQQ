package top.focess.qq.api.util;

import java.util.concurrent.TimeoutException;

/**
 * Thrown to indicate {@link IOHandler} has waited for more than 10 minutes to get input String
 */
public class InputTimeoutException extends TimeoutException {
    /**
     * Constructs a InputTimeoutException
     */
    public InputTimeoutException() {
        super("IOHandler has waited for more than 10 minutes to get input string.");
    }
}
