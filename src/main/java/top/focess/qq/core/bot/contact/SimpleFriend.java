package top.focess.qq.core.bot.contact;

import org.jetbrains.annotations.NotNull;
import top.focess.qq.api.bot.Bot;
import top.focess.qq.api.bot.contact.Friend;

public class SimpleFriend extends SimpleSpeaker implements Friend {
    private final String name;
    private final String avatarUrl;
    private final String rawName;

    public SimpleFriend(final Bot bot, final long id, final String name, final String rawName, final String avatarUrl) {
        super(bot, id);
        this.name = name;
        this.rawName = rawName;
        this.avatarUrl = avatarUrl;
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public String getRawName() {
        return this.rawName;
    }

    @NotNull
    @Override
    public String getAvatarUrl() {
        return this.avatarUrl;
    }

    @Override
    public void delete() {
        this.getBot().deleteFriend(this);
    }
}
