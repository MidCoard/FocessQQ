package top.focess.qq.api.bot.message.raw;

import net.mamoe.mirai.message.data.PlainText;

public class TextMessage extends Message {

    private final String text;

    public TextMessage(String text) {
        this.text = text;
    }

    @Override
    public net.mamoe.mirai.message.data.Message toMiraiMessage() {
        return new PlainText(text);
    }
}
