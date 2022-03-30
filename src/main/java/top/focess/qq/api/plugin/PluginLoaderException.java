package top.focess.qq.api.plugin;

/**
 * Thrown to indicate none-MainPlugin plugin is not loaded by PluginClassLoader
 */
public class PluginLoaderException extends IllegalStateException {
    /**
     * Constructs a PluginLoaderException
     * @param name the name of the plugin
     */
    public PluginLoaderException(final String name) {
        super("Plugin " + name + " is not loaded by PluginClassLoader.");
    }
}
