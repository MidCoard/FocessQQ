package com.focess.api.exceptions;

/**
 * Thrown to indicate there is an existed plugin named this name
 */
public class PluginDuplicateException extends RuntimeException {

    /**
     * Constructs a PluginDuplicateException
     *
     * @param name the name of the duplicated plugin
     */
    public PluginDuplicateException(String name) {
        super("Plugin " + name + " is duplicated.");
    }
}
