package top.focess.qq.api.net.packet;

/**
 * Used to create receive-packet time for client and send-packet time for server.
 */
public class WaitPacket extends ClientPacket{

    public static final int PACKET_ID = 10;

    /**
     * Constructs a WaitPacket
     * @param clientId the client id
     * @param token the client token
     */
    public WaitPacket(final int clientId, final String token) {
        super(clientId, token);
    }

    @Override
    public int getId() {
        return PACKET_ID;
    }
}
