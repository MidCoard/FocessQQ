package top.focess.qq.api.bot.message;

import org.jetbrains.annotations.NotNull;

import java.util.Iterator;
import java.util.stream.Stream;

public class MessageChain extends Message implements Iterable<Message> {

    public MessageChain(net.mamoe.mirai.message.data.MessageChain message) {
        super(message);
    }

    @NotNull
    @Override
    public Iterator<Message> iterator() {
        return ((net.mamoe.mirai.message.data.MessageChain)this.message).stream().map(Message::new).iterator();
    }

    public Stream<Message> stream() {
        return ((net.mamoe.mirai.message.data.MessageChain)this.message).stream().map(Message::new);
    }

    public String toMiraiCode() {
        return ((net.mamoe.mirai.message.data.MessageChain)this.message).serializeToMiraiCode();
    }

    public Message get(int index) {
        return new Message(((net.mamoe.mirai.message.data.MessageChain)this.message).get(index));
    }

    public int size() {
        return ((net.mamoe.mirai.message.data.MessageChain)this.message).size();
    }
}
