package com.focess.api;


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
     * @throws com.focess.api.exceptions.PluginLoaderException if the classloader of the plugin is not {@link com.focess.commands.LoadCommand.PluginClassLoader}
     */
    public LazyPlugin(String name) {
        super(name);
    }

    /**
     * Provide a constructor to help {@link com.focess.api.annotation.PluginType} design.
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
