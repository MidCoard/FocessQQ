package top.focess.qq.core.bot.contact;

import org.jetbrains.annotations.NotNull;
import top.focess.qq.api.bot.Bot;
import top.focess.qq.api.bot.contact.Friend;
import top.focess.qq.core.permission.Permission;

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
        Permission.checkPermission(Permission.DELETE_FRIEND);
        this.getBot().deleteFriend(this);
    }
}
