package top.focess.qq.core.plugin;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;

public interface FieldAnnotationHandler {

    void handle(Field field, Annotation annotation, PluginClassLoader pluginClassLoader);
}
