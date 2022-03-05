package top.focess.qq.api.net;

import top.focess.qq.api.plugin.Plugin;

/**
 * The class is used to handle packet.
 */
public interface Receiver {

    /**
     * Close the receiver.
     *
     * @return true if there is some resources not closed before, false otherwise
     */
    boolean close();

    /**
     * Unregister the packet handlers of the plugin
     * @param plugin the plugin
     */
    void unregister(Plugin plugin);

    /**
     * Unregister all the packet handlers
     * @return true if there are some packet-handlers not belonging to MainPlugin not been unregistered, false otherwise
     */
    boolean unregisterAll();
}
