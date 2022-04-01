package top.focess.qq.core.bot.contact;

import net.mamoe.mirai.contact.Contact;
import net.mamoe.mirai.message.data.Image;
import net.mamoe.mirai.utils.ExternalResource;
import org.jetbrains.annotations.NotNull;
import top.focess.qq.api.bot.Bot;
import top.focess.qq.api.bot.contact.Transmitter;
import top.focess.qq.api.bot.message.Message;

public abstract class SimpleTransmitter extends SimpleContact implements Transmitter {


    public SimpleTransmitter(final Bot bot, final Contact contact) {
        super(bot, contact);
    }

    @Override
    public void sendMessage(final String message) {
        this.contact.sendMessage(message);
    }

    @Override
    public void sendMessage(@NotNull final Message message) {
        this.contact.sendMessage(message.getNativeMessage());
    }


    @Override
    public Image uploadImage(final ExternalResource resource) {
        return this.contact.uploadImage(resource);
    }
}
