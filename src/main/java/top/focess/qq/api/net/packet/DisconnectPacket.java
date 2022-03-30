package top.focess.qq.api.net.packet;

/**
 * Used to tell server the connection has lost.
 */
public class DisconnectPacket extends ClientPacket {

    public static final int PACKET_ID = 5;

    /**
     * Constructs a DisconnectedPacket
     *
     * @param clientId the client id
     * @param token    the client token
     */
    public DisconnectPacket(final int clientId, final String token) {
        super(clientId, token);
    }

    @Override
    public int getId() {
        return PACKET_ID;
    }
}
