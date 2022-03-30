package top.focess.qq.api.bot.message;

public class Message {

    protected final net.mamoe.mirai.message.data.Message message;

    public Message(final net.mamoe.mirai.message.data.Message message) {
        this.message = message;
    }

    public static Message of(final net.mamoe.mirai.message.data.Message message) {
        return new Message(message);
    }

    public net.mamoe.mirai.message.data.Message getNativeMessage() {
        return this.message;
    }

    @Override
    public String toString() {
        return this.message.contentToString();
    }

    public Message plus(final Message message) {
        return new Message(this.message.plus(message.message));
    }
}
