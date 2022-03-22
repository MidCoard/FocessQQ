package top.focess.qq.api.bot.message.raw;

public class AtAll extends Message {

    @Override
    public net.mamoe.mirai.message.data.Message toMiraiMessage() {
        return net.mamoe.mirai.message.data.AtAll.INSTANCE;
    }
}
