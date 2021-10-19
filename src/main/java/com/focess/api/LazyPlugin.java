package com.focess.api;

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

    @Override
    public void enable() {
    }

    @Override
    public void disable() {
    }
}
