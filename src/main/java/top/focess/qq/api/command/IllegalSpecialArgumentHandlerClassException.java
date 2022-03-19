package top.focess.qq.api.command;

/**
 * Thrown to indicate this class is an illegal SpecialArgumentHandler class
 */
public class IllegalSpecialArgumentHandlerClassException extends IllegalArgumentException {

    /**
     * Constructs a new IllegalSpecialArgumentHandlerClassException
     * @param c the illegal special argument handler class
     * @param e the cause
     */
    public IllegalSpecialArgumentHandlerClassException(Class<? extends SpecialArgumentHandler> c, Exception e) {
        super("The class " + c.getName() + " is an illegal SpecialArgumentHandler class.",e);
    }

    public IllegalSpecialArgumentHandlerClassException(Class<?> c) {
        super("The class " + c.getName() + " is an illegal SpecialArgumentHandler class.");
    }
}
