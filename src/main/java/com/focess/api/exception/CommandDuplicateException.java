package com.focess.api.exception;

import com.focess.api.command.Command;

public class CommandDuplicateException extends RuntimeException{
    public CommandDuplicateException(String name) {
        super("Command " + name + " is duplicated.");
    }
}
