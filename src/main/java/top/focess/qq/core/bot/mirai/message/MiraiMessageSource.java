package top.focess.qq.core.bot.mirai.message;

import top.focess.qq.api.bot.message.MessageSource;

public class MiraiMessageSource extends MessageSource {

    private final net.mamoe.mirai.message.data.MessageSource source;

    private MiraiMessageSource(net.mamoe.mirai.message.data.MessageSource source) {
        this.source = source;
    }

    public static MessageSource of(net.mamoe.mirai.message.data.MessageSource source) {
        return new MiraiMessageSource(source);
    }

    @Override
    public long getSender() {
        return this.source.getFromId();
    }

    @Override
    public long getTarget() {
        return this.source.getTargetId();
    }

    @Override
    public long getBotId() {
        return this.source.getBotId();
    }

    @Override
    public int getTime() {
        return this.source.getTime();
    }

    @Override
    public int[] getIds() {
        return this.source.getIds();
    }

    @Override
    public int[] getInternalIds() {
        return this.source.getInternalIds();
    }
}
