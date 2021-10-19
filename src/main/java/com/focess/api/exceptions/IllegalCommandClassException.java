package com.focess.api.exceptions;

/**
 * Thrown to indicate this class is not an illegal Command class
 */
public class IllegalCommandClassException extends RuntimeException {
    /**
     * Constructs a IllegalCommandClassException
     *
     * @param c the illegal command class
     */
    public IllegalCommandClassException(Class<?> c) {
        super("The class " + c.getName() + " is not an illegal Command class.");
    }
}
