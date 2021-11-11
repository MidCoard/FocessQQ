package com.focess.api.net.packet;

import com.focess.api.net.PacketPreCodec;

/**
 * Codec for SidedConnectPacket.
 */
public class SidedConnectPacketCodec extends PacketCodec<SidedConnectPacket>{

    @Override
    public SidedConnectPacket readPacket(PacketPreCodec packetPreCodec) {
        return new SidedConnectPacket(packetPreCodec.readString());
    }

    @Override
    public void writePacket(SidedConnectPacket packet, PacketPreCodec packetPreCodec) {
        packetPreCodec.writeString(packet.getName());
    }
}
