package com.focess.api.exceptions;

public class PluginDuplicateException extends RuntimeException {

    public PluginDuplicateException(String name) {
        super("Plugin " + name + " is duplicated.");
    }
}
