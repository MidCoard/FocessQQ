package top.focess.qq.api.net.packet;

import top.focess.qq.api.net.PacketPreCodec;

/**
 * Codec for ConnectPacket.
 */
public class ConnectPacketCodec extends PacketCodec<ConnectPacket>{
    @Override
    public ConnectPacket readPacket(final PacketPreCodec packetPreCodec) {
        final String host = packetPreCodec.readString();
        final int port = packetPreCodec.readInt();
        final String name = packetPreCodec.readString();
        return new ConnectPacket(host,port,name);
    }

    @Override
    public void writePacket(final ConnectPacket packet, final PacketPreCodec packetPreCodec) {
        packetPreCodec.writeString(packet.getHost());
        packetPreCodec.writeInt(packet.getPort());
        packetPreCodec.writeString(packet.getName());
    }
}
