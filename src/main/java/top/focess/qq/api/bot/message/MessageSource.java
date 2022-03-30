package top.focess.qq.api.bot.message;

public class MessageSource {

    private final net.mamoe.mirai.message.data.MessageSource source;

    public MessageSource(final net.mamoe.mirai.message.data.MessageSource source) {
        this.source = source;
    }

    public net.mamoe.mirai.message.data.MessageSource getNativeSource() {
        return this.source;
    }

    public long getSender() {
        return this.source.getFromId();
    }

    public long getTarget() {
        return this.source.getTargetId();
    }

    public long getBotId() {
        return this.source.getBotId();
    }

    public int getTime() {
        return this.source.getTime();
    }

    public int[] getIds() {
        return this.source.getIds();
    }

    public int[] getInternalIds() {
        return this.source.getInternalIds();
    }
}
