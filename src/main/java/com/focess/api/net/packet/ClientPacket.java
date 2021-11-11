package com.focess.api.net.packet;

/**
 * The class indicates that this packet is for client side.
 */
public abstract class ClientPacket extends Packet{

    /**
     * The client id
     */
    private final int clientId;

    /**
     * The client token
     */
    private final String token;

    /**
     * Constructs a ClientPacket
     *
     * @param clientId the client id
     * @param token the client token
     */
    public ClientPacket(int clientId, String token) {
        this.clientId = clientId;
        this.token = token;
    }

    public int getClientId() {
        return clientId;
    }

    public String getToken() {
        return token;
    }
}
