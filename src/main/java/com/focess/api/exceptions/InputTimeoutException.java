package com.focess.api.exceptions;

public class InputTimeoutException extends RuntimeException {
    public InputTimeoutException() {
        super("CommandSender has waited for more than 10 minutes to get input string.");
    }
}
