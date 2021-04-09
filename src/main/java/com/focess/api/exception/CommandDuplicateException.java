package com.focess.api.exception;

public class CommandDuplicateException extends RuntimeException {
    public CommandDuplicateException(String name) {
        super("Command " + name + " is duplicated.");
    }
}
