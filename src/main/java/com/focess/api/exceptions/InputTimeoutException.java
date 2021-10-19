package com.focess.api.exceptions;

/**
 * Thrown to indicate {@link com.focess.api.util.IOHandler} has waited for more than 10 minutes to get input String
 */
public class InputTimeoutException extends RuntimeException {
    /**
     * Constructs a InputTimeoutException
     */
    public InputTimeoutException() {
        super("IOHandler has waited for more than 10 minutes to get input string.");
    }
}
