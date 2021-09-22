package com.focess.api.exceptions;

public class PluginLoaderException extends RuntimeException {
    public PluginLoaderException(String name) {
        super("Plugin " + name + " is not loaded by PluginClassLoader.");
    }
}
