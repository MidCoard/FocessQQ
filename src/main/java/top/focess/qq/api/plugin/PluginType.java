package top.focess.qq.api.plugin;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Represent this class is a Plugin. It means that this class must extend Plugin class.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface PluginType {

    /**
     * Set the dependent plugins for the plugin
     *
     * @return the dependent plugins or {} if there is no dependent
     */
    @Deprecated
    String[] depend() default {};

    /**
     * Set the name of this plugin
     *
     * @return the name of the plugin
     */
    @Deprecated
    String name() default "";

    /**
     * Set the author of this plugin
     *
     * @return the author of the plugin
     */
    @Deprecated
    String author() default "MidCoard";

    /**
     * Set the version of this plugin
     *
     * @return the version of the plugin
     */
    @Deprecated
    String version() default "1.0.0";

}
