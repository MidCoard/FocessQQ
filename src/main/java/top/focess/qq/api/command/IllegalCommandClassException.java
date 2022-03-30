package top.focess.qq.api.command;

/**
 * Thrown to indicate this class is an illegal Command class
 */
public class IllegalCommandClassException extends IllegalArgumentException {
    /**
     * Constructs a IllegalCommandClassException
     *
     * @param c the illegal command class
     */
    public IllegalCommandClassException(final Class<?> c) {
        super("The class " + c.getName() + " is an illegal Command class.");
    }
}
