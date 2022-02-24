package com.focess.api.event.plugin;

import com.focess.api.Plugin;
import com.focess.api.event.ListenerHandler;


/**
 * Called when a plugin is unloaded
 */
public class PluginUnloadEvent extends PluginEvent{

    private static final ListenerHandler LISTENER_HANDLER = new ListenerHandler();


    /**
     * Constructs a PluginUnloadEvent
     *
     * @param plugin the plugin
     */
    public PluginUnloadEvent(Plugin plugin) {
        super(plugin);
    }

}
