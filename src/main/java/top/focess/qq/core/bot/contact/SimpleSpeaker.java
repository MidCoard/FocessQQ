package top.focess.qq.core.bot.contact;

import net.mamoe.mirai.contact.AudioSupported;
import top.focess.qq.api.bot.Bot;
import top.focess.qq.api.bot.Speaker;

public abstract class SimpleSpeaker extends SimpleTransmitter implements Speaker {

    private final AudioSupported audioSupported;

    public SimpleSpeaker(Bot bot, AudioSupported contact) {
        super(bot, contact);
        this.audioSupported = contact;
    }

    public AudioSupported getNativeContact() {
        return this.audioSupported;
    }
}