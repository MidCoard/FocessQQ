package top.focess.qq.api.plugin;

import org.jetbrains.annotations.NotNull;

/**
 * Thrown to indicate this class is an illegal Plugin class
 */
public class IllegalPluginClassException extends IllegalArgumentException {
    /**
     * Constructs a IllegalPluginClassException
     *
     * @param c the illegal plugin class
     */
    public IllegalPluginClassException(@NotNull final Class<?> c) {
        super("The class " + c.getName() + " is an illegal Plugin class");
    }
}
