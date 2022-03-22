package top.focess.qq.core.bot;

import net.mamoe.mirai.contact.Contact;
import net.mamoe.mirai.message.data.Image;
import net.mamoe.mirai.message.data.Message;
import net.mamoe.mirai.utils.ExternalResource;
import top.focess.qq.api.bot.Bot;
import top.focess.qq.api.bot.Transmitter;

public abstract class SimpleTransmitter extends SimpleContact implements Transmitter {


    public SimpleTransmitter(Bot bot, Contact contact) {
        super(bot, contact);
    }

    @Override
    public void sendMessage(String message) {
        //todo message system
        this.contact.sendMessage(message);
    }

    @Override
    public void sendMessage(Message message) {
        this.contact.sendMessage(message);
    }


    @Override
    public Image uploadImage(ExternalResource resource) {
        return this.contact.uploadImage(resource);
    }
}
