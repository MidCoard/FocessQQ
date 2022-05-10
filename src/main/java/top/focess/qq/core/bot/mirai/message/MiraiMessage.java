package top.focess.qq.core.bot.mirai.message;

import net.mamoe.mirai.message.code.MiraiCode;
import top.focess.qq.api.bot.message.Message;

import java.util.Collections;

public class MiraiMessage implements Message {

    private final net.mamoe.mirai.message.data.Message message;

    public MiraiMessage(net.mamoe.mirai.message.data.Message message) {
        this.message = message;
    }

    @Override
    public String toString() {
        return this.message.contentToString();
    }

    @Override
    public String toMiraiCode() {
        return MiraiCode.serializeToMiraiCode(Collections.singleton(this.message));
    }

    public net.mamoe.mirai.message.data.Message getMessage() {
        return this.message;
    }
}
