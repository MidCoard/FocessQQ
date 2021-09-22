package com.focess.api.exceptions;

import com.focess.api.Plugin;

public class PluginLoadException extends RuntimeException {

    public PluginLoadException(Class<? extends Plugin> cls,Exception e) {
        super("Something wrong in loading Plugin " + cls.getName() + ".",e);
    }
}
