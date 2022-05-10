package top.focess.qq.api.bot.message;

import com.google.common.collect.Lists;
import org.jetbrains.annotations.NotNull;

import java.util.Iterator;
import java.util.List;
import java.util.stream.Stream;

/**
 * Represents a message chain.
 */
public class MessageChain implements Message,Iterable<Message> {

    private final List<Message> messageList;

    public MessageChain(@NotNull Message message) {
        if (message instanceof MessageChain)
            this.messageList = Lists.newArrayList(((MessageChain) message).messageList);
         else this.messageList = Lists.newArrayList(message);
    }

    MessageChain(Message... messages) {
        this.messageList = Lists.newArrayList(messages);
    }

    MessageChain(MessageChain messageChain, Message message) {
        this.messageList = Lists.newArrayList(messageChain.messageList);
        this.messageList.add(message);
    }

    MessageChain(Message message, MessageChain messageChain) {
        this.messageList = Lists.newArrayList(message);
        this.messageList.addAll(messageChain.messageList);
    }

    /**
     * Get the iterator of this message chain
     * @return the iterator of this message chain
     */
    @NotNull
    @Override
    public Iterator<Message> iterator() {
        return this.messageList.iterator();
    }

    /**
     * Get the stream of this message chain
     * @return the stream of this message chain
     */
    public Stream<Message> stream() {
        return this.messageList.stream();
    }

    /**
     * Get the message at the specified index
     * @param index the specified index
     * @return the message at the specified index
     */
    public Message get(final int index) {
        return this.messageList.get(index);
    }

    /**
     * Get the size of this message chain
     * @return the size of this message chain
     */
    public int size() {
        return this.messageList.size();
    }

    @Override
    public String toString() {
        return this.messageList.toString();
    }

    /**
     * Indicates whether this message chain is empty
     * @return true if this message chain is empty, false otherwise
     */
    public boolean isEmpty() {
        return this.messageList.isEmpty();
    }

    @Override
    public String toMiraiCode() {
        //todo
        return null;
    }

}
