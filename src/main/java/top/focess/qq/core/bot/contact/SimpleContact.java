package top.focess.qq.core.bot.contact;

import top.focess.qq.api.bot.Bot;
import top.focess.qq.api.bot.contact.Contact;

public abstract class SimpleContact implements Contact {
    private final Bot bot;
    private final long id;

    public SimpleContact(final Bot bot, final long id) {
        this.bot = bot;
        this.id = id;
    }

    @Override
    public long getId() {
        return this.id;
    }

    @Override
    public Bot getBot() {
        return this.bot;
    }
}
