package top.focess.qq.api.command.converter;

import top.focess.qq.api.command.data.DataBuffer;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Represent this field is a DataConverter
 */
@Target(value = ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface DataConverterType {

    Class<? extends DataBuffer<?>> buffer();
}
