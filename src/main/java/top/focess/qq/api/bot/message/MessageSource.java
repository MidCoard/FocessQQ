package top.focess.qq.api.bot.message;

/**
 * Represents a message source.
 */
public class MessageSource {

    /**
     * The native message source
     */
    private final net.mamoe.mirai.message.data.MessageSource source;

    /**
     * Constructs a message source
     * @param source the native message source
     */
    public MessageSource(final net.mamoe.mirai.message.data.MessageSource source) {
        this.source = source;
    }

    public net.mamoe.mirai.message.data.MessageSource getNativeSource() {
        return this.source;
    }

    /**
     * Get the sender id
     * @return the sender id
     */
    public long getSender() {
        return this.source.getFromId();
    }

    /**
     * Get the target id
     * @return the target id
     */
    public long getTarget() {
        return this.source.getTargetId();
    }

    /**
     * Get the bot id
     * @return the bot id
     */
    public long getBotId() {
        return this.source.getBotId();
    }

    /**
     * Get the sending time
     * @return the sending time
     */
    public int getTime() {
        return this.source.getTime();
    }

    /**
     * Get the message ids
     * @return the message ids
     */
    public int[] getIds() {
        return this.source.getIds();
    }

    /**
     * Get the message internal ids
     * @return the message internal ids
     */
    public int[] getInternalIds() {
        return this.source.getInternalIds();
    }
}
