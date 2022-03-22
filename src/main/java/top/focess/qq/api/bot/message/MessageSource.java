package top.focess.qq.api.bot.message;

public class MessageSource {

    private final net.mamoe.mirai.message.data.MessageSource source;

    public MessageSource(net.mamoe.mirai.message.data.MessageSource source) {
        this.source = source;
    }

    public net.mamoe.mirai.message.data.MessageSource getNativeSource() {
        return this.source;
    }
}
