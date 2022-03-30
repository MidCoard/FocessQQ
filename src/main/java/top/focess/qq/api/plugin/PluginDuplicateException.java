package top.focess.qq.api.plugin;

/**
 * Thrown to indicate there is an existed plugin named this name
 */
public class PluginDuplicateException extends IllegalStateException {

    /**
     * Constructs a PluginDuplicateException
     *
     * @param name    the name of the duplicated plugin
     * @param message the message
     */
    public PluginDuplicateException(final String name, final String message) {
        super("Plugin " + name + " is duplicated. " + message);
    }
}
