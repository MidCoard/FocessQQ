package com.focess.api.exceptions;

public class CommandDuplicateException extends RuntimeException {
    public CommandDuplicateException(String name) {
        super("Command " + name + " is duplicated.");
    }
}
