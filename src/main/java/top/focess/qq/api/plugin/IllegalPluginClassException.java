package top.focess.qq.api.plugin;
/**
 * Thrown to indicate this class is not an illegal Plugin class
 */
public class IllegalPluginClassException extends IllegalArgumentException {
    /**
     * Constructs a IllegalPluginClassException
     * @param c the illegal plugin class
     */
    public IllegalPluginClassException(Class<?> c) {
        super("The class " + c.getName() + " is an illegal Plugin class");
    }
}
