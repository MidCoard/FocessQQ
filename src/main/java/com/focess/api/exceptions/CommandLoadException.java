package com.focess.api.exceptions;

import com.focess.api.command.Command;

/**
 * Thrown to indicate there is any exception thrown in the initializing process
 */
public class CommandLoadException extends RuntimeException {
    /**
     * Constructs a CommandLoadException
     * @param c the class of the command
     * @param e the exception thrown in the initializing process
     */
    public CommandLoadException(Class<? extends Command> c, Exception e) {
        super("Something wrong in loading Command " + c.getName() + ".",e);
    }
}
