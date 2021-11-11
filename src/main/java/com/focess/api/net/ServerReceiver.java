package com.focess.api.net;

import com.focess.api.net.packet.Packet;

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

}
