package top.focess.qq.api.net.packet;

import org.jetbrains.annotations.Nullable;
import top.focess.qq.api.net.PacketPreCodec;

/**
 * Codec for ClientPackPacket.
 */
public class ClientPackPacketCodec extends PacketCodec<ClientPackPacket>{

    @Nullable
    @Override
    public ClientPackPacket readPacket(final PacketPreCodec packetPreCodec) {
        final int clientId = packetPreCodec.readInt();
        final String token = packetPreCodec.readString();
        final Packet packet = packetPreCodec.readPacket();
        if (packet == null)
            return null;
        return new ClientPackPacket(clientId,token,packet);
    }

    @Override
    public void writePacket(final ClientPackPacket packet, final PacketPreCodec packetPreCodec) {
        packetPreCodec.writeInt(packet.getClientId());
        packetPreCodec.writeString(packet.getToken());
        packetPreCodec.writePacket(packet.getPacket());
    }
}
