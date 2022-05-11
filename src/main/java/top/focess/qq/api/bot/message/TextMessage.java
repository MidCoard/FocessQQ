package top.focess.qq.api.bot.message;

public class TextMessage implements Message {

    private final String text;

    public TextMessage(String text) {
        this.text = text;
    }

    @Override
    public String toString() {
        return this.text;
    }

    public String getText() {
        return this.text;
    }
}
