package com.focess.api.exception;

public class PluginLoaderException extends RuntimeException {
    public PluginLoaderException(String name) {
        super("Plugin " + name + " is not loaded by PluginClassLoader.");
    }
}
