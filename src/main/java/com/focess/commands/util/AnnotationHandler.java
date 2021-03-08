package com.focess.commands.util;

import java.lang.annotation.Annotation;
import com.focess.commands.LoadCommand.PluginClassLoader;

public interface AnnotationHandler {
    void handle(Class c, Annotation annotation, PluginClassLoader classLoader);
}