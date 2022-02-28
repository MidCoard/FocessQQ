package top.focess.qq.api.net.packet;

/**
 * Used to pack package sent by client.
 */
public class ClientPackPacket extends ClientPacket{

    public static final int PACKET_ID = 7;
    /**
     * The packet sent by client
     */
    private final Packet packet;

    /**
     * Constructs a ClientPackPacket
     * @param clientId the client id
     * @param token the client token
     * @param packet the packet sent by client
     */
    public ClientPackPacket(int clientId, String token,Packet packet) {
        super(clientId, token);
        this.packet = packet;
    }

    @Override
    public int getId() {
        return PACKET_ID;
    }

    public Packet getPacket() {
        return packet;
    }
}
