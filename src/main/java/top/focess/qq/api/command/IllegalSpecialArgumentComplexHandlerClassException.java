package top.focess.qq.api.command;

/**
 * Thrown to indicate this class is an illegal SpecialArgumentHandler class
 */
public class IllegalSpecialArgumentComplexHandlerClassException extends IllegalArgumentException {

    /**
     * Constructs a new IllegalSpecialArgumentHandlerClassException
     * @param c the illegal special argument handler class
     * @param e the cause
     */
    public IllegalSpecialArgumentComplexHandlerClassException(final Class<? extends SpecialArgumentComplexHandler> c, final Exception e) {
        super("The class " + c.getName() + " is an illegal SpecialArgumentComplexHandler class.",e);
    }

    public IllegalSpecialArgumentComplexHandlerClassException(final Class<?> c) {
        super("The class " + c.getName() + " is an illegal SpecialArgumentComplexHandler class.");
    }
}
