package com.focess.api.net.packet;

import com.focess.api.net.PacketPreCodec;
/**
 * Codec for DisconnectedPacket.
 */
public class DisconnectedPacketCodec extends PacketCodec<DisconnectedPacket>{
    @Override
    public DisconnectedPacket readPacket(PacketPreCodec packetPreCodec) {
        return new DisconnectedPacket();
    }

    @Override
    public void writePacket(DisconnectedPacket packet, PacketPreCodec packetPreCodec) {}
}
