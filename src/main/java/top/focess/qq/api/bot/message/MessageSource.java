package top.focess.qq.api.bot.message;

/**
 * Represents a message source.
 */
public abstract class MessageSource {

    /**
     * Get the sender id
     * @return the sender id
     */
    public abstract long getSender();

    /**
     * Get the target id
     * @return the target id
     */
    public abstract long getTarget();

    /**
     * Get the bot id
     * @return the bot id
     */
    public abstract long getBotId();

    /**
     * Get the sending time
     * @return the sending time
     */
    public abstract int getTime();

    /**
     * Get the message ids
     * @return the message ids
     */
    public abstract int[] getIds();

    /**
     * Get the message internal ids
     * @return the message internal ids
     */
    public abstract int[] getInternalIds();
}
