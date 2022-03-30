package top.focess.qq.api.command.converter;

import top.focess.qq.api.command.DataConverter;

/**
 * Thrown to indicate this class is an illegal DataConverter class
 */
public class IllegalDataConverterClassException extends IllegalArgumentException {

    /**
     * Constructs a IllegalDataConverterClassException
     * @param c the illegal DataConverter class
     * @param e the cause
     */
    public IllegalDataConverterClassException(final Class<? extends DataConverter> c, final Exception e) {
        super("The class " + c.getName() + " is an illegal DataConverter class", e);
    }

    public IllegalDataConverterClassException(final Class<?> c) {
        super("The class " + c.getName() + " is an illegal DataConverter class");
    }
}
