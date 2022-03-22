package top.focess.qq.api.bot.message.raw;

public class At extends Message {

    private final long id;

    public At(long id) {
        this.id = id;
    }

    @Override
    public net.mamoe.mirai.message.data.Message toMiraiMessage() {
        return new net.mamoe.mirai.message.data.At(id);
    }
}
