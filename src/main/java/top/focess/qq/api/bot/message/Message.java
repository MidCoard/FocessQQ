package top.focess.qq.api.bot.message;

public class Message {

    protected final net.mamoe.mirai.message.data.Message message;

    public Message(net.mamoe.mirai.message.data.Message message) {
        this.message = message;
    }

    public net.mamoe.mirai.message.data.Message getNativeMessage() {
        return this.message;
    }

    @Override
    public String toString() {
        return this.message.contentToString();
    }
}
