package com.focess.api.net;

import com.focess.api.net.packet.Packet;

/**
 * The socket receiver for client.
 */
public interface ClientReceiver extends Receiver {

    /**
     * Send the packet to the server
     *
     * @param packet the packet
     */
    void sendPacket(Packet packet);

    /**
     * Register packet handler for server
     *
     * @param c the packet class
     * @param packHandler the packet handler
     * @param <T> the packet type
     */
    <T extends Packet> void registerPackHandler(Class<T> c,PackHandler<T> packHandler);

    /**
     * Get the name of the client
     *
     * @return the name of the client
     */
    String getName();

    /**
     * Get the target host of the client
     *
     * @return the target host of the client
     */
    String getHost();

    /**
     * Get the target port of the client
     *
     * @return the target port of the client
     */
    int getPort();

    /**
     * Indicate this client has connected to a server
     * @return true if the client has connected to a server, false otherwise
     */
    boolean isConnected();

    /**
     * Get the client id
     *
     * @return the client id
     */
    int getClientId();

    /**
     * Get the client token
     *
     * @return the client token
     */
    String getClientToken();
}
