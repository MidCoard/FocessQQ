package top.focess.qq.api.command.converter;

import top.focess.qq.api.command.DataConverter;

/**
 * Simplify the {@link DataConverter} class.
 * Implement the accept method. The accept method returns true if there is no exception in converting the String argument, false otherwise.
 *
 * @param <T> the target type
 */
public abstract class ExceptionDataConverter<T> extends DataConverter<T>{

    @Override
    protected boolean accept(String arg) {
        try {
            convert(arg);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
