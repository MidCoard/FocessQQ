package com.focess.api.event.plugin;

import com.focess.api.Plugin;

/**
 * Called when a plugin is loaded
 */
public class PluginLoadEvent extends PluginEvent{

    /**
     * Constructs a PluginLoadEvent
     *
     * @param plugin the plugin
     */
    public PluginLoadEvent(Plugin plugin) {
        super(plugin);
    }

}
