package top.focess.qq.api.net.packet;

import org.jetbrains.annotations.Nullable;
import top.focess.qq.api.net.PacketPreCodec;

/**
 * Codec for ServerPackPacket.
 */
public class ServerPackPacketCodec extends PacketCodec<ServerPackPacket>{
    @Nullable
    @Override
    public ServerPackPacket readPacket(PacketPreCodec packetPreCodec) {
        Packet packet = packetPreCodec.readPacket();
        if (packet == null)
            return null;
        return new ServerPackPacket(packet);
    }

    @Override
    public void writePacket(ServerPackPacket packet, PacketPreCodec packetPreCodec) {
        packetPreCodec.writePacket(packet.getPacket());
    }
}
