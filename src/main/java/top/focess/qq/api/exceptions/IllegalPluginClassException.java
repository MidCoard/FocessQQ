package top.focess.qq.api.exceptions;
/**
 * Thrown to indicate this class is not an illegal Plugin class
 */
public class IllegalPluginClassException extends RuntimeException {
    /**
     * Constructs a IllegalPluginClassException
     * @param c the illegal plugin class
     */
    public IllegalPluginClassException(Class<?> c) {
        super("The class " + c.getName() + " is not an illegal Plugin class");
    }
}
