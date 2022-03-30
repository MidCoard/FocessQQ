package top.focess.qq.api.net.packet;

import top.focess.qq.api.net.PacketPreCodec;

/**
 * Codec for DisconnectPacket.
 */
public class DisconnectPacketCodec extends PacketCodec<DisconnectPacket> {
    @Override
    public DisconnectPacket readPacket(final PacketPreCodec packetPreCodec) {
        final int clientId = packetPreCodec.readInt();
        final String token = packetPreCodec.readString();
        return new DisconnectPacket(clientId, token);
    }

    @Override
    public void writePacket(final DisconnectPacket packet, final PacketPreCodec packetPreCodec) {
        packetPreCodec.writeInt(packet.getClientId());
        packetPreCodec.writeString(packet.getToken());
    }
}
