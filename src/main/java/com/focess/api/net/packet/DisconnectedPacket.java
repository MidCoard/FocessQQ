package com.focess.api.net.packet;

/**
 * Used to tell client the connection has lost.
 */
public class DisconnectedPacket extends ServerPacket {

    public static final int PACKET_ID = 6;

    /**
     * Constructs a DisconnectedPacket
     */
    public DisconnectedPacket() {

    }
    @Override
    public int getId() {
        return PACKET_ID;
    }
}
