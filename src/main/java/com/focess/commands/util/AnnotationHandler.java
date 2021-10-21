package com.focess.commands.util;

import com.focess.commands.LoadCommand.PluginClassLoader;
import org.jetbrains.annotations.NotNull;

import java.lang.annotation.Annotation;

public interface AnnotationHandler {
    boolean handle(@NotNull Class<?> c, @NotNull Annotation annotation, @NotNull PluginClassLoader classLoader);
}