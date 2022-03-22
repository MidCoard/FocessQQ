package top.focess.qq.api.bot;

import net.mamoe.mirai.message.data.Image;
import net.mamoe.mirai.message.data.Message;
import net.mamoe.mirai.utils.ExternalResource;

public interface Transmitter extends Contact{

    @Deprecated
    void sendMessage(String message);

    @Deprecated
    void sendMessage(Message message);

    @Deprecated
    Image uploadImage(ExternalResource resource);
}
