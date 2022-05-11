package top.focess.qq.core.bot.mirai.message;

import top.focess.qq.api.bot.message.Message;

public class MiraiMessage implements Message {

    private final net.mamoe.mirai.message.data.Message message;

    public MiraiMessage(final net.mamoe.mirai.message.data.Message message) {
        this.message = message;
    }

    @Override
    public String toString() {
        return this.message.contentToString();
    }

    public net.mamoe.mirai.message.data.Message getMessage() {
        return this.message;
    }
}
