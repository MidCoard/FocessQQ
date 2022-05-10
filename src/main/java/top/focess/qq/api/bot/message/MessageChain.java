package top.focess.qq.api.bot.message;

import com.google.common.collect.Lists;
import net.mamoe.mirai.message.code.MiraiCode;
import org.jetbrains.annotations.NotNull;
import top.focess.qq.core.bot.mirai.message.MiraiMessage;

import java.util.Iterator;
import java.util.List;
import java.util.stream.Stream;

/**
 * Represents a message chain.
 */
public class MessageChain implements Message,Iterable<Message> {

    private final List<Message> messageList;

    public MessageChain(@NotNull final Message message) {
        if (message instanceof MessageChain)
            this.messageList = Lists.newArrayList(((MessageChain) message).messageList);
         else this.messageList = Lists.newArrayList(message);
    }

    MessageChain(final Message... messages) {
        this.messageList = Lists.newArrayList(messages);
    }

    MessageChain(final MessageChain messageChain, final Message message) {
        this.messageList = Lists.newArrayList(messageChain.messageList);
        this.messageList.add(message);
    }

    MessageChain(final Message message, final MessageChain messageChain) {
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
        if (this.isEmpty())
            return "";
        final Message first = this.messageList.get(0);
        if (first instanceof MiraiMessage) {
            net.mamoe.mirai.message.data.Message message = ((MiraiMessage) first).getMessage();
            for (int i = 1; i < this.messageList.size(); i++) {
                final Message next = this.messageList.get(i);
                if (next instanceof MiraiMessage)
                    message = message.plus(((MiraiMessage) next).getMessage());
                else throw new IllegalArgumentException("MessageChain can only contain MiraiMessage");
            }
            return MiraiCode.serializeToMiraiCode(new net.mamoe.mirai.message.data.Message[]{message});
        }
        throw new UnsupportedOperationException("Not supported yet.");
    }

}
