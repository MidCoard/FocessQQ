package com.focess.api.net.packet;

import com.focess.api.net.PacketPreCodec;

/**
 * Codec for ServerPackPacket.
 */
public class ServerPackPacketCodec extends PacketCodec<ServerPackPacket>{
    @Override
    public ServerPackPacket readPacket(PacketPreCodec packetPreCodec) {
        Packet packet = packetPreCodec.readPacket();
        return new ServerPackPacket(packet);
    }

    @Override
    public void writePacket(ServerPackPacket packet, PacketPreCodec packetPreCodec) {
        packetPreCodec.writePacket(packet.getPacket());
    }
}
