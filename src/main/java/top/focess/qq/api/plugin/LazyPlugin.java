package top.focess.qq.api.plugin;


import top.focess.qq.api.util.version.Version;
import top.focess.qq.core.plugin.PluginClassLoader;
import top.focess.qq.api.exceptions.PluginLoaderException;

/**
 * This is lazy version of the plugin.
 * Do not need to implement {@link Plugin#enable()} and {@link Plugin#disable()}
 * @see Plugin
 */
public abstract class LazyPlugin extends Plugin{

    /**
     * Initialize a Plugin instance by its name.
     * Never instance it! It will be instanced when bot bootstraps automatically.
     *
     * @param name the plugin name
     * @throws PluginLoaderException if the classloader of the plugin is not {@link PluginClassLoader}
     */
    public LazyPlugin(String name, String author, Version version) {
        super(name,author, version);
    }

    /**
     * Provide a constructor to help {@link PluginType} design.
     * Never instance it! It will be instanced when bot bootstraps automatically.
     */
    public LazyPlugin(){
    }

    @Override
    public void enable() {
    }

    @Override
    public void disable() {
    }
}
