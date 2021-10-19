package com.focess.api.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Represent this class is a Plugin. It means that this class must extend Plugin class.
 */
@Target(value = ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface PluginType {

    /**
     * Set the dependent plugins for the plugin
     *
     * @return the dependent plugins or {} if there is no dependent
     */
    String[] loadAfter() default {};

    /**
     * Set the name of this plugin
     *
     * @return the name of the plugin
     */
    String name() default "";

}
