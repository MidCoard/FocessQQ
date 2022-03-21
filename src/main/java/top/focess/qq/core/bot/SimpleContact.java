package top.focess.qq.core.bot;

import top.focess.qq.api.bot.Bot;
import top.focess.qq.api.bot.Contact;

public abstract class SimpleContact implements Contact {

    private final Bot bot;
    protected final net.mamoe.mirai.contact.Contact contact;

    public SimpleContact(Bot bot, net.mamoe.mirai.contact.Contact contact) {
        this.bot = bot;
        this.contact = contact;
    }

    @Override
    public long getId() {
        return this.contact.getId();
    }

    @Override
    public Bot getBot() {
        return this.bot;
    }
}
