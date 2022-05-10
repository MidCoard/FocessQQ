package top.focess.qq.core.bot.mirai.message;

import top.focess.qq.api.bot.message.Image;

public class MiraiImage extends MiraiMessage implements Image {
    public MiraiImage(final net.mamoe.mirai.message.data.Image image) {
        super(image);
    }
}
