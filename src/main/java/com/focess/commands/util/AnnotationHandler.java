package com.focess.commands.util;

import com.focess.commands.LoadCommand.PluginClassLoader;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.lang.annotation.Annotation;

public interface AnnotationHandler {
    boolean handle(@NonNull Class<?> c, @NonNull Annotation annotation, @NonNull PluginClassLoader classLoader);
}