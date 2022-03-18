package top.focess.qq.api.net.packet;

import org.checkerframework.checker.nullness.qual.Nullable;
import top.focess.qq.api.net.PacketPreCodec;

/**
 * The codec for special packet type.
 *
 * @param <T> the packet type
 */
public abstract class PacketCodec<T extends Packet>{

    /**
     * Read the special packet from precodec
     *
     * @param packetPreCodec the precodec
     * @return the packet
     */
    @Nullable
    public abstract T readPacket(PacketPreCodec packetPreCodec);

    /**
     * Write the packet to the precodec
     *
     * @param packet the packet
     * @param packetPreCodec the precodec
     */
    public abstract void writePacket(T packet, PacketPreCodec packetPreCodec);
}
