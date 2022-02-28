package top.focess.qq.api.net.packet;

import top.focess.qq.api.net.PacketPreCodec;

/**
 * Codec for HeartPacket.
 */
public class HeartPacketCodec extends PacketCodec<HeartPacket>{

    @Override
    public HeartPacket readPacket(PacketPreCodec packetPreCodec) {
        int clientId = packetPreCodec.readInt();
        String token = packetPreCodec.readString();
        long time = packetPreCodec.readLong();
        return new HeartPacket(clientId,token,time);
    }

    @Override
    public void writePacket(HeartPacket packet, PacketPreCodec packetPreCodec) {
        packetPreCodec.writeInt(packet.getClientId());
        packetPreCodec.writeString(packet.getToken());
        packetPreCodec.writeLong(packet.getTime());
    }
}
