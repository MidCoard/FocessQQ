package top.focess.qq.core.bot.contact;

import org.jetbrains.annotations.NotNull;
import top.focess.qq.api.bot.Bot;
import top.focess.qq.api.bot.contact.Transmitter;
import top.focess.qq.api.bot.message.Image;
import top.focess.qq.api.bot.message.Message;

import java.io.InputStream;

public abstract class SimpleTransmitter extends SimpleContact implements Transmitter {


    public SimpleTransmitter(final Bot bot, final long id) {
        super(bot, id);
    }

    @Override
    public void sendMessage(final String message) {
        this.getBot().sendMessage(this,message);
    }

    @Override
    public void sendMessage(@NotNull final Message message) {
        this.getBot().sendMessage(this,message);
    }


    @Override
    public Image uploadImage(InputStream inputStream) {
        return this.getBot().uploadImage(this, inputStream);
    }
}
