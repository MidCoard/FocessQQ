package top.focess.qq.api.net.packet;

/**
 * Used to send String message.
 */
public class MessagePacket extends Packet{


    public static final int PACKET_ID = 1;
    /**
     * The message
     */
    private final String message;

    /**
     * Constructs a MessagePacket
     *
     * @param message the message
     */
    public MessagePacket(final String message){
        this.message = message;
    }

    @Override
    public int getId() {
        return PACKET_ID;
    }

    public String getMessage() {
        return this.message;
    }
}
