package top.focess.qq.core.bot.contact;

import org.checkerframework.checker.nullness.qual.Nullable;
import top.focess.qq.api.bot.Bot;
import top.focess.qq.api.bot.contact.Speaker;
import top.focess.qq.api.bot.message.Audio;

import java.io.InputStream;

public abstract class SimpleSpeaker extends SimpleTransmitter implements Speaker {

    public SimpleSpeaker(final Bot bot, final long id) {
        super(bot, id);
    }

    @Override
    public @Nullable Audio uploadAudio(final InputStream inputStream) {
        return this.getBot().uploadAudio(this, inputStream);
    }
}
