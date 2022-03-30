package top.focess.qq.api.net.packet;

/**
 * Used to tell server the connection is not lost.
 */
public class HeartPacket extends ClientPacket{

    public static final int PACKET_ID = 2;
    private final long time;

    /**
     * Constructs a HeartPacket
     *
     * @param clientId the client id
     * @param token the client token
     * @param time the client time
     */
    public HeartPacket(final int clientId, final String token, final long time) {
        super(clientId, token);
        this.time = time;
    }

    @Override
    public int getId() {
        return PACKET_ID;
    }

    public long getTime() {
        return this.time;
    }
}
