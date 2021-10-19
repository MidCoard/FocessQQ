package com.focess.api.exceptions;

import com.focess.api.Plugin;

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

    /**
     * Constructs a PluginLoaderException
     *
     * @param c the class of the plugin
     */
    public PluginLoaderException(Class<? extends Plugin> c) {
        super("Plugin " + c.getName() + " is not loaded by PluginClassLoader.");
    }
}
