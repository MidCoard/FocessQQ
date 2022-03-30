package top.focess.qq.api.net.packet;

import top.focess.qq.api.net.PacketPreCodec;

/**
 * Codec for WaitPacket.
 */
public class WaitPacketCodec extends PacketCodec<WaitPacket> {

    @Override
    public WaitPacket readPacket(final PacketPreCodec packetPreCodec) {
        final int clientId = packetPreCodec.readInt();
        final String token = packetPreCodec.readString();
        return new WaitPacket(clientId, token);
    }

    @Override
    public void writePacket(final WaitPacket packet, final PacketPreCodec packetPreCodec) {
        packetPreCodec.writeInt(packet.getClientId());
        packetPreCodec.writeString(packet.getToken());
    }
}
