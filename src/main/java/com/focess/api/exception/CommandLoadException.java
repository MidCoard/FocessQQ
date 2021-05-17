package com.focess.api.exception;

import com.focess.api.command.Command;

public class CommandLoadException extends RuntimeException {
    public CommandLoadException(Class<? extends Command> c) {
        super("Something wrong in loading Command " + c.getName() + ".");
    }
}
