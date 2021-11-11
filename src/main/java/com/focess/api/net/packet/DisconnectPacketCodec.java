package com.focess.api.net.packet;

import com.focess.api.net.PacketPreCodec;

/**
 * Codec for DisconnectPacket.
 */
public class DisconnectPacketCodec extends PacketCodec<DisconnectPacket>{
    @Override
    public DisconnectPacket readPacket(PacketPreCodec packetPreCodec) {
        int clientId = packetPreCodec.readInt();
        String token = packetPreCodec.readString();
        return new DisconnectPacket(clientId,token);
    }

    @Override
    public void writePacket(DisconnectPacket packet, PacketPreCodec packetPreCodec) {
        packetPreCodec.writeInt(packet.getClientId());
        packetPreCodec.writeString(packet.getToken());
    }
}
