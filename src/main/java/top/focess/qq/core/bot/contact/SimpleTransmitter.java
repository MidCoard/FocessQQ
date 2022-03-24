package top.focess.qq.core.bot.contact;

import net.mamoe.mirai.contact.Contact;
import net.mamoe.mirai.message.data.Image;
import net.mamoe.mirai.utils.ExternalResource;
import top.focess.qq.api.bot.Bot;
import top.focess.qq.api.bot.contact.Transmitter;
import top.focess.qq.api.bot.message.Message;

public abstract class SimpleTransmitter extends SimpleContact implements Transmitter {


    public SimpleTransmitter(Bot bot, Contact contact) {
        super(bot, contact);
    }

    @Override
    public void sendMessage(String message) {
        this.contact.sendMessage(message);
    }

    @Override
    public void sendMessage(Message message) {
        this.contact.sendMessage(message.getNativeMessage());
    }


    @Override
    public Image uploadImage(ExternalResource resource) {
        return this.contact.uploadImage(resource);
    }
}
