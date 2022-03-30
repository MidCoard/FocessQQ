package top.focess.qq.api.net.packet;

import top.focess.qq.api.net.PacketPreCodec;

/**
 * Codec for ConnectedPacket.
 */
public class ConnectedPacketCodec extends PacketCodec<ConnectedPacket>{
    @Override
    public ConnectedPacket readPacket(final PacketPreCodec packetPreCodec) {
        final int clientId = packetPreCodec.readInt();
        final String token = packetPreCodec.readString();
        return new ConnectedPacket(clientId,token);
    }

    @Override
    public void writePacket(final ConnectedPacket packet, final PacketPreCodec packetPreCodec) {
        packetPreCodec.writeInt(packet.getClientId());
        packetPreCodec.writeString(packet.getToken());
    }
}
