package top.focess.qq.api.net.packet;

/**
 * Used to connect to the server.
 */
public class SidedConnectPacket extends Packet{

    public static final int PACKET_ID = 9;
    /**
     * The client name
     */
    private final String name;

    /**
     * Constructs a SidedConnectPacket
     * @param name the client name
     */
    public SidedConnectPacket(final String name) {
        this.name = name;
    }

    public String getName() {
        return this.name;
    }

    @Override
    public int getId() {
        return PACKET_ID;
    }
}
