package com.focess.api.net;

/**
 * Represents a FocessSocket. This class is used to handle socket.
 */
public interface Socket {

    /**
     * Register packet receiver for this socket
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
     */
    void close();

}
