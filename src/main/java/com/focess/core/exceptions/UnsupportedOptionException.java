package com.focess.core.exceptions;

public class UnsupportedOptionException extends RuntimeException {
    public UnsupportedOptionException(String option) {
        super("The option " + option + " is not supported.");
    }
}
