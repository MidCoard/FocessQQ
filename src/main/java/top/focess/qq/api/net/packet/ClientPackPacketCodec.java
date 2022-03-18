package top.focess.qq.api.net.packet;

import org.jetbrains.annotations.Nullable;
import top.focess.qq.api.net.PacketPreCodec;

/**
 * Codec for ClientPackPacket.
 */
public class ClientPackPacketCodec extends PacketCodec<ClientPackPacket>{

    @Nullable
    @Override
    public ClientPackPacket readPacket(PacketPreCodec packetPreCodec) {
        int clientId = packetPreCodec.readInt();
        String token = packetPreCodec.readString();
        Packet packet = packetPreCodec.readPacket();
        if (packet == null)
            return null;
        return new ClientPackPacket(clientId,token,packet);
    }

    @Override
    public void writePacket(ClientPackPacket packet, PacketPreCodec packetPreCodec) {
        packetPreCodec.writeInt(packet.getClientId());
        packetPreCodec.writeString(packet.getToken());
        packetPreCodec.writePacket(packet.getPacket());
    }
}
