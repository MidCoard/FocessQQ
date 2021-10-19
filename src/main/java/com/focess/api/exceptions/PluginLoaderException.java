package com.focess.api.exceptions;

/**
 * Thrown to indicate none-MainPlugin plugin is not loaded by PluginClassLoader
 */
public class PluginLoaderException extends RuntimeException {
    /**
     * Constructs a PluginLoaderException
     * @param name the name of the plugin
     */
    public PluginLoaderException(String name) {
        super("Plugin " + name + " is not loaded by PluginClassLoader.");
    }
}
