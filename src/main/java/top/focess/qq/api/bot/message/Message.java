package top.focess.qq.api.bot.message;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

/**
 * Represents a message.
 */
public class Message {

    /**
     * The native message.
     */
    protected final net.mamoe.mirai.message.data.Message message;

    /**
     * Constructs a Message.
     * @param message the native message
     */
    protected Message(final net.mamoe.mirai.message.data.Message message) {
        this.message = message;
    }

    /**
     * Wrap a message
     *
     * @param message the native message
     * @return the wrapped message
     */
    @NotNull
    @Contract(value = "_ -> new", pure = true)
    public static Message of(final net.mamoe.mirai.message.data.Message message) {
        return new Message(message);
    }

    public net.mamoe.mirai.message.data.Message getNativeMessage() {
        return this.message;
    }

    /**
     * Get the string representation of this message.
     * @return the string representation of this message
     */
    @Override
    public String toString() {
        return this.message.contentToString();
    }

    /**
     * Append a message to the end of this message.
     * @param message the message to append
     * @return the new message
     */
    public Message plus(@NotNull final Message message) {
        return new Message(this.message.plus(message.message));
    }
}
