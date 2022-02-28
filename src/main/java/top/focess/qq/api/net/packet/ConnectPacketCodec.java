package top.focess.qq.api.net.packet;

import top.focess.qq.api.net.PacketPreCodec;

/**
 * Codec for ConnectPacket.
 */
public class ConnectPacketCodec extends PacketCodec<ConnectPacket>{
    @Override
    public ConnectPacket readPacket(PacketPreCodec packetPreCodec) {
        String host = packetPreCodec.readString();
        int port = packetPreCodec.readInt();
        String name = packetPreCodec.readString();
        return new ConnectPacket(host,port,name);
    }

    @Override
    public void writePacket(ConnectPacket packet, PacketPreCodec packetPreCodec) {
        packetPreCodec.writeString(packet.getHost());
        packetPreCodec.writeInt(packet.getPort());
        packetPreCodec.writeString(packet.getName());
    }
}
