package com.focess.api.exceptions;

/**
 * Thrown to indicate there
 */
public class CommandDuplicateException extends RuntimeException {
    public CommandDuplicateException(String name) {
        super("Command " + name + " is duplicated.");
    }
}
