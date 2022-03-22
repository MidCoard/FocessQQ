package top.focess.qq.core.bot.contact;

import net.mamoe.mirai.contact.Friend;
import net.mamoe.mirai.contact.Member;
import net.mamoe.mirai.contact.Stranger;
import org.jetbrains.annotations.Nullable;
import top.focess.qq.api.bot.Bot;
import top.focess.qq.api.bot.Contact;

public abstract class SimpleContact implements Contact {

    private final Bot bot;
    protected final net.mamoe.mirai.contact.Contact contact;

    public SimpleContact(Bot bot, net.mamoe.mirai.contact.Contact contact) {
        this.bot = bot;
        this.contact = contact;
    }

    @Nullable
    public static Contact get(Bot bot, net.mamoe.mirai.contact.Contact contact) {
        if (contact == null)
            return null;
        if (contact.getBot().getId() != bot.getId())
            return null;
        if (contact instanceof Stranger)
            return SimpleStranger.get(bot,(Stranger) contact);
        if (contact instanceof Member)
            return SimpleMember.get(bot, (Member) contact);
        if (contact instanceof Friend)
            return SimpleFriend.get(bot, (Friend) contact);
        return null;
    }

    @Override
    public long getId() {
        return this.contact.getId();
    }

    @Override
    public Bot getBot() {
        return this.bot;
    }

    public net.mamoe.mirai.contact.Contact getNativeContact() {
        return this.contact;
    }
}
