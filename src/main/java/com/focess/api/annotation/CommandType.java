package com.focess.api.annotation;

import com.focess.api.Plugin;

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
     * Define what Plugin class it belongs to
     *
     * @return the Plugin Class instance
     */
    Class<? extends Plugin> plugin();
}
