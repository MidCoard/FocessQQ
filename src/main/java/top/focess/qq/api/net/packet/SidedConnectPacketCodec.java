package top.focess.qq.api.net.packet;

import top.focess.qq.api.net.PacketPreCodec;

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
