package top.focess.qq.api.plugin;


import top.focess.qq.core.plugin.PluginClassLoader;

/**
 * This is lazy version of the plugin.
 * Do not need to implement {@link Plugin#enable()} and {@link Plugin#disable()}
 *
 * @see Plugin
 */
public abstract class LazyPlugin extends Plugin {

    /**
     * Initialize a Plugin instance.
     * Never instance it! It will be instanced when bot bootstraps automatically.
     *
     * @throws PluginLoaderException if the classloader of the plugin is not {@link PluginClassLoader} and the plugin is not the MainPlugin.
     * @throws PluginDuplicateException if the plugin is already loaded
     * @throws IllegalStateException if the plugin is newed in runtime
     */
    public LazyPlugin() {}

    @Override
    public void enable() {
    }

    @Override
    public void disable() {
    }
}
