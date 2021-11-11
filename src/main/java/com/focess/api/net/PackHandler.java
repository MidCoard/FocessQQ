package com.focess.api.net;

import com.focess.api.net.packet.Packet;

/**
 * Represents a packet handler to define how to handle packet.
 *
 * @param <T> the packet type
 * This is a functional interface whose functional method is {@link PackHandler#handle(Packet)}
 */
public interface PackHandler<T extends Packet> {

    /**
     * Used to handle the packet
     *
     * @param packet the packet
     */
    void handle(T packet);
}
