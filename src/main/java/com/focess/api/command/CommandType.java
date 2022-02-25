package com.focess.api.command;

import com.focess.api.plugin.Plugin;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Represent this class is a Command. It means that this class must extend Command class.
 */
@Target(value = ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface CommandType {

    /**
     * Set the plugin the command belongs to
     *
     * @return the plugin the command belongs to
     */
    Class<? extends Plugin> plugin();

    /**
     * Set the name of the command
     *
     * @return the name of the command
     */
    String name() default "";

    /**
     * Set the aliases of the command
     *
     * @return the aliases of the command
     */
    String[] aliases() default {};
}
