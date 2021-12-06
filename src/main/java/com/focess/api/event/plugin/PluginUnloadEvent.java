package com.focess.api.event.plugin;

import com.focess.api.Plugin;


/**
 * Called when a plugin is unloaded
 */
public class PluginUnloadEvent extends PluginEvent{

    /**
     * Constructs a PluginUnloadEvent
     *
     * @param plugin the plugin
     */
    public PluginUnloadEvent(Plugin plugin) {
        super(plugin);
    }

}
