package com.focess.api.net;

import com.focess.api.net.packet.Packet;
import org.jetbrains.annotations.Nullable;

/**
 * The socket receiver for server.
 */
public interface ServerReceiver extends Receiver{

    /**
     * Send packet to the special client
     *
     * @param client the client name
     * @param packet the packet
     */
    void sendPacket(String client, Packet packet);

    /**
     * Register packet handler for special client
     *
     * @param client the client name
     * @param c the packet class
     * @param packHandler the packet handler
     * @param <T> the packet type
     */
    <T extends Packet> void registerPackHandler(String client,Class<T> c,PackHandler<T> packHandler);

    /**
     * Indicate the client is connected to server
     *
     * @param client the client name
     * @return true if the client is connected to server, false otherwise
     */
    boolean isConnected(String client);

    /**
     * Get the client by given name
     *
     * @param name the client name
     * @return the client
     */
    @Nullable
    Client getClient(String name);

}
