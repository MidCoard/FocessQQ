package top.focess.qq.api.net.packet;

/**
 * Used to pack package sent by server.
 */
public class ServerPackPacket extends ServerPacket {

    public static final int PACKET_ID = 8;
    /**
     * The packet sent by server
     */
    private final Packet packet;

    /**
     * Constructs a ServerPackPacket
     *
     * @param packet the packet sent by server
     */
    public ServerPackPacket(final Packet packet) {
        this.packet = packet;
    }

    @Override
    public int getId() {
        return PACKET_ID;
    }

    public Packet getPacket() {
        return this.packet;
    }
}
