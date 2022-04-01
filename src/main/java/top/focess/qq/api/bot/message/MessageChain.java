package top.focess.qq.api.bot.message;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.Iterator;
import java.util.stream.Stream;

/**
 * Represents a message chain.
 */
public class MessageChain extends Message implements Iterable<Message> {

    /**
     * Constructs a MessageChain
     * @param message the native message
     */
    private MessageChain(final net.mamoe.mirai.message.data.MessageChain message) {
        super(message);
    }

    /**
     * Get the iterator of this message chain
     * @return the iterator of this message chain
     */
    @NotNull
    @Override
    public Iterator<Message> iterator() {
        return ((net.mamoe.mirai.message.data.MessageChain) this.message).stream().map(Message::new).iterator();
    }

    /**
     * Get the stream of this message chain
     * @return the stream of this message chain
     */
    public Stream<Message> stream() {
        return ((net.mamoe.mirai.message.data.MessageChain) this.message).stream().map(Message::new);
    }

    /**
     * Get the message as MiraiCode
     * @return the message as MiraiCode
     */
    public String toMiraiCode() {
        return ((net.mamoe.mirai.message.data.MessageChain) this.message).serializeToMiraiCode();
    }

    /**
     * Get the message at the specified index
     * @param index the specified index
     * @return the message at the specified index
     */
    public Message get(final int index) {
        return new Message(((net.mamoe.mirai.message.data.MessageChain) this.message).get(index));
    }

    /**
     * Get the size of this message chain
     * @return the size of this message chain
     */
    public int size() {
        return ((net.mamoe.mirai.message.data.MessageChain) this.message).size();
    }

    /**
     * Wrap a message chain
     * @param message the native message chain
     * @return the wrapped message chain
     */
    @NotNull
    @Contract("_ -> new")
    public static MessageChain of(net.mamoe.mirai.message.data.MessageChain message) {
        return new MessageChain(message);
    }
}
