package top.focess.qq.api.command;

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
