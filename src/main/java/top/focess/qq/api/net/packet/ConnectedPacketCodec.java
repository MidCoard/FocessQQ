package top.focess.qq.api.net.packet;

import top.focess.qq.api.net.PacketPreCodec;

/**
 * Codec for ConnectedPacket.
 */
public class ConnectedPacketCodec extends PacketCodec<ConnectedPacket>{
    @Override
    public ConnectedPacket readPacket(PacketPreCodec packetPreCodec) {
        int clientId = packetPreCodec.readInt();
        String token = packetPreCodec.readString();
        return new ConnectedPacket(clientId,token);
    }

    @Override
    public void writePacket(ConnectedPacket packet, PacketPreCodec packetPreCodec) {
        packetPreCodec.writeInt(packet.getClientId());
        packetPreCodec.writeString(packet.getToken());
    }
}
