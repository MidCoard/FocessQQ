package top.focess.qq.core.bot.contact;

import com.google.common.collect.Maps;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import top.focess.qq.api.bot.Bot;
import top.focess.qq.api.bot.contact.Friend;

import java.util.Map;

public class SimpleFriend extends SimpleSpeaker implements Friend {

    private static final Map<Long, Map<Long, SimpleFriend>> FRIEND_MAP = Maps.newConcurrentMap();
    private final net.mamoe.mirai.contact.Friend nativeFriend;

    private SimpleFriend(final Bot bot, final net.mamoe.mirai.contact.Friend nativeFriend) {
        super(bot, nativeFriend);
        this.nativeFriend = nativeFriend;
    }

    @Nullable
    public static Friend get(final Bot bot, @Nullable final net.mamoe.mirai.contact.Friend nativeFriend) {
        if (nativeFriend == null)
            return null;
        if (bot.getId() != nativeFriend.getBot().getId())
            return null;
        return FRIEND_MAP.computeIfAbsent(bot.getId(), k -> Maps.newConcurrentMap()).computeIfAbsent(nativeFriend.getId(), k -> new SimpleFriend(bot, nativeFriend));
    }

    public static void remove(final Bot bot) {
        FRIEND_MAP.remove(bot.getId());
    }

    @Override
    public String getName() {
        return this.nativeFriend.getRemark();
    }

    @Override
    public String getRawName() {
        return this.nativeFriend.getNick();
    }

    @NotNull
    @Override
    public String getAvatarUrl() {
        return this.nativeFriend.getAvatarUrl();
    }

    @Override
    public void delete() {
        this.nativeFriend.delete();
    }
}
