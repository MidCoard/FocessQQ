package top.focess.qq.api.bot.message;

import org.jetbrains.annotations.NotNull;

/**
 * Represents a message.
 */
public abstract class Message {

    /**
     * Get the string representation of this message.
     * @return the string representation of this message
     */
    @Override
    public abstract String toString();

    @Deprecated
    public abstract String toMiraiCode();

    /**
     * Append a message to the end of this message.
     * @param message the message to append
     * @return the new message
     */
    public Message plus(@NotNull final Message message) {
        if (this instanceof MessageChain)
            return new MessageChain((MessageChain)this, message);
        if (message instanceof MessageChain)
            return new MessageChain(this, (MessageChain)message);
        return new MessageChain(this,message);
    }
}
