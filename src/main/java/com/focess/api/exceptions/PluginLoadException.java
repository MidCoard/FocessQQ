package com.focess.api.exceptions;

import com.focess.api.plugin.Plugin;

/**
 * Thrown to indicate there is any exception thrown in the initializing process
 */
public class PluginLoadException extends RuntimeException {

    /**
     * Constructs a PluginLoadException
     * @param c the class of the plugin
     * @param e the exception
     */
    public PluginLoadException(Class<? extends Plugin> c,Exception e) {
        super("Something wrong in loading Plugin " + c.getName() + ".",e);
    }
}
