package top.focess.qq.api.net;

import top.focess.qq.api.plugin.Plugin;

/**
 * Represents a FocessSocket. This class is used to handle socket.
 */
public interface Socket {

    /**
     * Register packet receiver for this socket
     *
     * @param receiver the packet receiver for this socket
     */
    void registerReceiver(Receiver receiver);

    /**
     * Indicate this socket contains server side receiver
     *
     * @return true if it contains server side receiver, false otherwise
     */
    boolean containsServerSide();

    /**
     * Indicate this socket contains client side receiver
     *
     * @return true if it contains client side receiver, false otherwise
     */
    boolean containsClientSide();

    /**
     * Close the socket
     *
     * @return true if there is some resources not closed before, false otherwise
     */
    boolean close();

    /**
     * Unregister the packet-handlers of the plugin
     *
     * @param plugin the plugin
     */
    void unregister(Plugin plugin);
}
