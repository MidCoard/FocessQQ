package top.focess.qq.api.net.packet;

import org.jetbrains.annotations.Nullable;
import top.focess.qq.api.net.PacketPreCodec;

/**
 * Codec for ServerPackPacket.
 */
public class ServerPackPacketCodec extends PacketCodec<ServerPackPacket> {
    @Nullable
    @Override
    public ServerPackPacket readPacket(final PacketPreCodec packetPreCodec) {
        final Packet packet = packetPreCodec.readPacket();
        if (packet == null)
            return null;
        return new ServerPackPacket(packet);
    }

    @Override
    public void writePacket(final ServerPackPacket packet, final PacketPreCodec packetPreCodec) {
        packetPreCodec.writePacket(packet.getPacket());
    }
}
