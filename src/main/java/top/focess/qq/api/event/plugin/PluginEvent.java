package top.focess.qq.api.event.plugin;

import top.focess.qq.api.event.Event;
import top.focess.qq.api.event.ListenerHandler;
import top.focess.qq.api.plugin.Plugin;

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
    public PluginEvent(final Plugin plugin) {
        this.plugin = plugin;
    }

    public Plugin getPlugin() {
        return this.plugin;
    }
}
