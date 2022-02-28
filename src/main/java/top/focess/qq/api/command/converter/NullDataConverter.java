package top.focess.qq.api.command.converter;

import top.focess.qq.api.command.DataConverter;

/**
 * Simplify the {@link DataConverter} class.
 * Implement the accept method. The accept method returns true if the result of converting the String argument is not null, false otherwise.
 *
 * @param <T> the target type
 */
public abstract class NullDataConverter<T> extends DataConverter<T>{

    @Override
    protected boolean accept(String arg) {
        return convert(arg) != null;
    }
}
