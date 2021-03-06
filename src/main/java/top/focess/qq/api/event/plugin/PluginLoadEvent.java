package top.focess.qq.api.event.plugin;

import top.focess.qq.api.event.ListenerHandler;
import top.focess.qq.api.plugin.Plugin;

/**
 * Called when a plugin is loaded
 */
public class PluginLoadEvent extends PluginEvent {

    private static final ListenerHandler LISTENER_HANDLER = new ListenerHandler();


    /**
     * Constructs a PluginLoadEvent
     *
     * @param plugin the plugin
     */
    public PluginLoadEvent(final Plugin plugin) {
        super(plugin);
    }

}
