package top.focess.qq.api.command;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Represent this field is a special argument handler.
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface SpecialArgumentType {

    /**
     * The name of the special argument
     * @return the name of the special argument
     */
    String name();
}
