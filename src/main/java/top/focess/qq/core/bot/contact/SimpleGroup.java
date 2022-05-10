package top.focess.qq.core.bot.contact;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnmodifiableView;
import top.focess.qq.api.bot.Bot;
import top.focess.qq.api.bot.contact.Group;
import top.focess.qq.api.bot.contact.Member;

import java.util.List;

public class SimpleGroup extends SimpleSpeaker implements Group {

    private final String name;

    private final String avatarUrl;

    public SimpleGroup(final Bot bot, final long id, String name, String avatarUrl) {
        super(bot, id);
        this.name = name;
        this.avatarUrl = avatarUrl;
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public void quit() {
        this.getBot().quitGroup(this);
    }

    @Nullable
    @Override
    public Member getMember(final long id) {
        return this.getBot().getMember(this, id);
    }

    @Override
    public Member getMemberOrFail(final long id) {
        return this.getBot().getMemberOrFail(this, id);
    }

    @Override
    @UnmodifiableView
    public @NonNull List<Member> getMembers() {
        return this.getBot().getMembers(this);
    }

    @Override
    public Member getAsMember() {
        return this.getBot().getAsMember(this);
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

}
