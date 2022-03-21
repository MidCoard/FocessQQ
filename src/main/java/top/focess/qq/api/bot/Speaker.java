package top.focess.qq.api.bot;

import net.mamoe.mirai.message.data.Message;

public interface Speaker extends Contact{

    void sendMessage(String message);

    void sendMessage(Message message);
}
