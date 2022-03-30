package top.focess.qq.api.net.packet;

import top.focess.qq.api.net.PacketPreCodec;

/**
 * Codec for MessagePacket.
 */
public class MessagePacketCodec extends PacketCodec<MessagePacket>{

    @Override
    public MessagePacket readPacket(final PacketPreCodec packetPreCodec) {
        return new MessagePacket(packetPreCodec.readString());
    }

    @Override
    public void writePacket(final MessagePacket packet, final PacketPreCodec packetPreCodec) {
        packetPreCodec.writeString(packet.getMessage());
    }
}
