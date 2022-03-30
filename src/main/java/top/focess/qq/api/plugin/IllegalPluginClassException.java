package top.focess.qq.api.plugin;
/**
 * Thrown to indicate this class is an illegal Plugin class
 */
public class IllegalPluginClassException extends IllegalArgumentException {
    /**
     * Constructs a IllegalPluginClassException
     * @param c the illegal plugin class
     */
    public IllegalPluginClassException(final Class<?> c) {
        super("The class " + c.getName() + " is an illegal Plugin class");
    }
}
