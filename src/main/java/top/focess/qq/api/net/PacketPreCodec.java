package top.focess.qq.api.net;

import top.focess.qq.Main;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.jetbrains.annotations.Nullable;
import top.focess.qq.api.net.packet.*;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

/**
 * This class is used to prehandle the packet.
 */
public class PacketPreCodec {

    private static final Map<Integer, PacketCodec<? extends Packet>> PACKET_CODECS = Maps.newHashMap();
    static{
        PacketPreCodec.registerPacketCodec(MessagePacket.PACKET_ID,new MessagePacketCodec());
        PacketPreCodec.registerPacketCodec(HeartPacket.PACKET_ID,new HeartPacketCodec());
        PacketPreCodec.registerPacketCodec(ConnectPacket.PACKET_ID,new ConnectPacketCodec());
        PacketPreCodec.registerPacketCodec(ConnectedPacket.PACKET_ID,new ConnectedPacketCodec());
        PacketPreCodec.registerPacketCodec(DisconnectPacket.PACKET_ID,new DisconnectPacketCodec());
        PacketPreCodec.registerPacketCodec(DisconnectedPacket.PACKET_ID,new DisconnectedPacketCodec());
        PacketPreCodec.registerPacketCodec(ClientPackPacket.PACKET_ID,new ClientPackPacketCodec());
        PacketPreCodec.registerPacketCodec(ServerPackPacket.PACKET_ID,new ServerPackPacketCodec());
        PacketPreCodec.registerPacketCodec(SidedConnectPacket.PACKET_ID,new SidedConnectPacketCodec());
        PacketPreCodec.registerPacketCodec(WaitPacket.PACKET_ID,new WaitPacketCodec());
    }

    private final List<Byte> data = Lists.newArrayList();

    private int pointer = 0;

    /**
     * Read a integer
     * @return the integer read from precodec
     */
    public int readInt() {
        int r = 0;
        for (int i = 0; i < 4; i++)
            r += Byte.toUnsignedInt(data.get(pointer++)) << (i * 8);
        return r;
    }

    /**
     * Read a long
     * @return the long read from precodec
     */
    public long readLong() {
        long r = 0L;
        for (int i = 0; i < 8; i++)
            r += Byte.toUnsignedLong(data.get(pointer++)) << (i * 8L);
        return r;
    }

    /**
     * Write a integer
     * @param v the integer
     */
    public void writeInt(int v) {
        for (int i = 0; i < 4; i++) {
            data.add((byte) (v & 0xFF));
            v >>>= 8;
        }
    }

    /**
     * Write a long
     * @param v the long
     */
    public void writeLong(long v) {
        for (int i = 0; i < 8; i++) {
            data.add((byte) (v & 0xFFL));
            v >>>= 8;
        }
    }

    /**
     * Write a string
     * @param v the string
     */
    public void writeString(String v) {
        writeInt(v.length());
        for (byte b : v.getBytes(StandardCharsets.UTF_8))
            data.add(b);
    }

    /**
     * Read a string
     * @return the string read from precodec
     */
    public String readString() {
        int length = readInt();
        byte[] bytes = new byte[length];
        for (int i = 0; i < length; i++)
            bytes[i] = data.get(pointer++);
        return new String(bytes, StandardCharsets.UTF_8);
    }

    /**
     * Write a float
     *
     * @param v the float
     */
    public void writeFloat(float v) {
        writeInt(Float.floatToIntBits(v));
    }

    /**
     * Read a float
     * @return the float read from precodec
     */
    public float readFloat() {
        return Float.intBitsToFloat(readInt());
    }

    /**
     * Write a double
     * @param v the double
     */
    public void writeDouble(double v) {
        writeLong(Double.doubleToLongBits(v));
    }

    /**
     * Read a double
     *
     * @return the double read from precodec
     */
    public double readDouble() {
        return Double.longBitsToDouble(readLong());
    }

    /**
     * Read a byte
     *
     * @return the byte read from precodec
     */
    public byte readByte() {
        return this.data.get(pointer++);
    }

    /**
     * Write a byte
     * @param b the byte
     */
    public void writeByte(byte b) {
        this.data.add(b);
    }

    /**
     * Read a short
     * @return the short read from precodec
     */
    public short readShort() {
        short r = 0;
        for (int i = 0; i < 2; i++)
            r += (short) Byte.toUnsignedInt(data.get(pointer++)) << (i * 8);
        return r;
    }

    /**
     * Write a short
     *
     * @param v the short
     */
    public void writeShort(short v) {
        for (int i = 0; i < 2; i++) {
            data.add((byte) (v & 0xFF));
            v >>>= 8;
        }
    }

    /**
     * Get all bytes of the packet
     * @return all bytes of the packet
     */
    public byte[] getBytes() {
        byte[] bytes = new byte[this.data.size()];
        for (int i = 0; i < this.data.size(); i++)
            bytes[i] = this.data.get(i);
        return bytes;
    }

    /**
     * Read a packet
     * @return the packet read from precodec
     */
    @Nullable
    public Packet readPacket() {
        int packetId;
        try {
            packetId = readInt();
        } catch (Exception e) {
            return null;
        }
        PacketCodec<? extends Packet> packetCodec = PACKET_CODECS.get(packetId);
        if (packetCodec != null)
            return packetCodec.readPacket(this);
        Main.getLogger().debugLang("unknown-packet", packetId);
        return null;
    }

    /**
     * Write a packet
     *
     * @param packet the packet
     * @param <T> the packet type
     * @return true if the packet has been written successfully, false otherwise
     */
    public <T extends Packet> boolean writePacket(T packet) {
        int packetId = packet.getId();
        PacketCodec<T> packetCodec = (PacketCodec<T>) PACKET_CODECS.get(packetId);
        if (packetCodec != null) {
            this.writeInt(packetId);
            packetCodec.writePacket(packet, this);
            return true;
        } else Main.getLogger().debugLang("unknown-packet", packetId);
        return false;
    }

    /**
     * Register the packet codec for the special packet id
     *
     * @param packetId the packet id
     * @param packetCodec the packet codec
     * @param <T>  the packet type
     */
    public static <T extends Packet> void registerPacketCodec(int packetId, PacketCodec<T> packetCodec) {
        PACKET_CODECS.put(packetId, packetCodec);
    }

    /**
     * Push the data to the precodec
     *
     * @param buffer the data
     * @param offset the offset of the data
     * @param length the length of the data
     * @see #push(byte[], int)
     */
    public void push(byte[] buffer, int offset, int length) {
        for (int i = offset; i < length; i++)
            data.add(buffer[i]);
    }

    /**
     * Push the data to the precodec
     *
     * @param buffer the data
     * @param length the length of the data
     * @see #push(byte[], int, int)
     */
    public void push(byte[] buffer, int length) {
        this.push(buffer,0,length);
    }
}
