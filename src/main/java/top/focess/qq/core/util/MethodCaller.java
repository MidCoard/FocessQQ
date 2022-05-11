package top.focess.qq.core.util;

import org.checkerframework.checker.nullness.qual.Nullable;
import top.focess.qq.FocessQQ;
import top.focess.qq.core.plugin.PluginCoreClassLoader;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class MethodCaller {

    @Nullable
    public static Class<?> getCallerClass() {
        final StackTraceElement[] stackTraceElements = Thread.currentThread().getStackTrace();
        if (stackTraceElements.length < 4)
            return null;
        try {
            return PluginCoreClassLoader.forName(stackTraceElements[3].getClassName());
        } catch (final ClassNotFoundException e) {
            return null;
        }
    }

    public static List<Class<?>> getAllCallerClass() {
        return Arrays.stream(Thread.currentThread().getStackTrace()).map(StackTraceElement::getClassName).map(i -> {
            try {
                return PluginCoreClassLoader.forName(i);
            } catch (ClassNotFoundException e) {
                FocessQQ.getLogger().thrLang("exception-get-null-caller-class",e);
                return null;
            }
        }).filter(Objects::nonNull).collect(Collectors.toList());
    }
}
