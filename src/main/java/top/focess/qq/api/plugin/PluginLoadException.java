package top.focess.qq.api.plugin;

import org.jetbrains.annotations.NotNull;

/**
 * Thrown to indicate there is any exception thrown in the initializing process
 */
public class PluginLoadException extends RuntimeException {

    /**
     * Constructs a PluginLoadException
     *
     * @param c the class of the plugin
     * @param e the exception
     */
    public PluginLoadException(@NotNull final Class<? extends Plugin> c, final Throwable e) {
        super("Something wrong in loading Plugin " + c.getName() + ".", e);
    }
}
