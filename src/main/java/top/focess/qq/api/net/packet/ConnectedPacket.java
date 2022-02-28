package top.focess.qq.api.net.packet;

/**
 * Used to tell client the id and the token
 */
public class ConnectedPacket extends ServerPacket {

    public static final int PACKET_ID = 4;
    /**
     * The client id
     */
    private final int clientId;
    /**
     * The token
     */
    private final String token;

    /**
     * Constructs a ConnectedPacket
     *
     * @param clientId the client id
     * @param token the token
     */
    public ConnectedPacket(int clientId,String token) {
        this.clientId = clientId;
        this.token = token;
    }

    @Override
    public int getId() {
        return PACKET_ID;
    }

    public int getClientId() {
        return clientId;
    }

    public String getToken() {
        return token;
    }
}
