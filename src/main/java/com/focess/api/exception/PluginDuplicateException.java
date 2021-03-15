package com.focess.api.exception;

public class PluginDuplicateException extends RuntimeException {

    public PluginDuplicateException(String name) {
        super("Plugin " + name + " is duplicated.");
    }
}
