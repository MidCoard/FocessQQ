package top.focess.qq.api.net.packet;

import top.focess.qq.api.net.PacketPreCodec;

/**
 * Codec for WaitPacket.
 */
public class WaitPacketCodec extends PacketCodec<WaitPacket>{

    @Override
    public WaitPacket readPacket(PacketPreCodec packetPreCodec) {
        int clientId = packetPreCodec.readInt();
        String token = packetPreCodec.readString();
        return new WaitPacket(clientId,token);
    }

    @Override
    public void writePacket(WaitPacket packet, PacketPreCodec packetPreCodec) {
        packetPreCodec.writeInt(packet.getClientId());
        packetPreCodec.writeString(packet.getToken());
    }
}
