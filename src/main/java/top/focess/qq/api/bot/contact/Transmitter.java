package top.focess.qq.api.bot.contact;

import net.mamoe.mirai.message.data.Image;
import net.mamoe.mirai.utils.ExternalResource;
import top.focess.qq.api.bot.message.Message;

public interface Transmitter extends Contact{

    void sendMessage(String message);

    void sendMessage(Message message);

    Image uploadImage(ExternalResource resource);
}
