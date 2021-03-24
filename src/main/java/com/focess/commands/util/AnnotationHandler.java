package com.focess.commands.util;

import java.lang.annotation.Annotation;
import com.focess.commands.LoadCommand.PluginClassLoader;
import org.checkerframework.checker.nullness.qual.NonNull;

public interface AnnotationHandler {
    boolean handle(@NonNull Class<?> c, @NonNull Annotation annotation,@NonNull PluginClassLoader classLoader);
}