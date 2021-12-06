package com.focess.api.event.plugin;

import com.focess.api.Plugin;
import com.focess.api.event.Event;
import com.focess.api.event.ListenerHandler;

/**
 * Called when a plugin triggers a special action
 */
public class PluginEvent extends Event {

    private static final ListenerHandler LISTENER_HANDLER = new ListenerHandler();

    /**
     * The plugin
     */
    private final Plugin plugin;

    /**
     * Constructs a PluginEvent
     *
     * @param plugin the plugin
     */
    public PluginEvent(Plugin plugin) {
        this.plugin = plugin;
    }

    public Plugin getPlugin() {
        return plugin;
    }
}
