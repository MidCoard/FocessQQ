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
    public IllegalSpecialArgumentComplexHandlerClassException(Class<? extends SpecialArgumentComplexHandler> c, Exception e) {
        super("The class " + c.getName() + " is an illegal SpecialArgumentComplexHandler class.",e);
    }

    public IllegalSpecialArgumentComplexHandlerClassException(Class<?> c) {
        super("The class " + c.getName() + " is an illegal SpecialArgumentComplexHandler class.");
    }
}
