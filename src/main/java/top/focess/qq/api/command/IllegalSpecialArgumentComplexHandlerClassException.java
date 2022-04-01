package top.focess.qq.api.command;

import org.jetbrains.annotations.NotNull;

/**
 * Thrown to indicate this class is an illegal SpecialArgumentHandler class
 */
public class IllegalSpecialArgumentComplexHandlerClassException extends IllegalArgumentException {

    /**
     * Constructs a new IllegalSpecialArgumentHandlerClassException
     *
     * @param c the illegal special argument handler class
     * @param e the cause
     */
    public IllegalSpecialArgumentComplexHandlerClassException(@NotNull final Class<? extends SpecialArgumentComplexHandler> c, final Exception e) {
        super("The class " + c.getName() + " is an illegal SpecialArgumentComplexHandler class.", e);
    }

    /**
     * Constructs a new IllegalSpecialArgumentHandlerClassException
     * @param c the illegal special argument handler class
     */
    public IllegalSpecialArgumentComplexHandlerClassException(@NotNull final Class<?> c) {
        super("The class " + c.getName() + " is an illegal SpecialArgumentComplexHandler class.");
    }
}
