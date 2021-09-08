package com.focess.api.exception;

import com.focess.api.Plugin;

public class PluginLoadException extends RuntimeException {

    public PluginLoadException(Class<? extends Plugin> cls,Exception e) {
        super("Something wrong in loading Plugin " + cls.getName() + ".",e);
    }
}
