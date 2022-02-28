package top.focess.qq.api.net.packet;

import top.focess.qq.api.net.PacketPreCodec;

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
