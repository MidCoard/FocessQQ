package top.focess.qq.core.bot.mirai.message;

import net.mamoe.mirai.message.data.PlainText;
import org.jetbrains.annotations.Nullable;
import top.focess.qq.api.bot.message.Message;
import top.focess.qq.api.bot.message.TextMessage;

public class MiraiMessageUtil {

    public static @Nullable MiraiMessage toMiraiMessage(final Message message) {
        if (message instanceof MiraiMessage)
            return (MiraiMessage) message;
        if (message instanceof TextMessage)
            return new MiraiMessage(new PlainText(((TextMessage) message).getText()));
        return null;
    }

    public static @Nullable net.mamoe.mirai.message.data.Message toNativeMessage(final Message message) {
        MiraiMessage miraiMessage = toMiraiMessage(message);
        if (miraiMessage != null)
            return miraiMessage.getMessage();
        return null;
    }
}
