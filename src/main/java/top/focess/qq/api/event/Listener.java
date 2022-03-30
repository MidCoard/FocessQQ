package top.focess.qq.api.event;

import top.focess.qq.api.plugin.Plugin;

/**
 * This class is used to indicate this is an event listener
 */
public interface Listener {

    /**
     * Get the plugin that this listener belongs to
     *
     * @return the plugin that this listener belongs to
     */
    default Plugin getPlugin() {
        return ListenerHandler.LISTENER_PLUGIN_MAP.get(this);
    }

    /**
     * Unregister this listener
     */
    default void unregister() {
    	ListenerHandler.unregister(this.getPlugin(),this);
    }
}
