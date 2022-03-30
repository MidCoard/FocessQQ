package top.focess.qq.api.net.packet;

/**
 * Used to connect to the server.
 */
public class ConnectPacket extends Packet{

    public static final int PACKET_ID = 3;
    /**
     * The client host
     */
    private final String host;
    /**
     * The client port
     */
    private final int port;
    /**
     * The client name
     */
    private final String name;

    /**
     * Constructs a ConnectPacket
     *
     * @param host the client host
     * @param port the client port
     * @param name the client name
     */
    public ConnectPacket(final String host, final int port, final String name) {
        this.host = host;
        this.port = port;
        this.name = name;
    }

    @Override
    public int getId() {
        return PACKET_ID;
    }

    public String getHost() {
        return this.host;
    }

    public int getPort() {
        return this.port;
    }

    public String getName() {
        return this.name;
    }
}
