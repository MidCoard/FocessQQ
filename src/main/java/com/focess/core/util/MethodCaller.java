package com.focess.core.util;

import com.focess.core.plugin.PluginCoreClassLoader;

public class MethodCaller {

    public static Class<?> getCallerClass() {
        StackTraceElement[] stackTraceElements = Thread.currentThread().getStackTrace();
        if (stackTraceElements.length < 4)
            return null;
        try {
            return PluginCoreClassLoader.forName(stackTraceElements[3].getClassName());
        } catch (ClassNotFoundException e) {
            return null;
        }
    }
}