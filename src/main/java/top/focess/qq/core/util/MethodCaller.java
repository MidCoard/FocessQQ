package top.focess.qq.core.util;

import org.checkerframework.checker.nullness.qual.Nullable;
import top.focess.qq.core.plugin.PluginCoreClassLoader;

public class MethodCaller {

    @Nullable
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
