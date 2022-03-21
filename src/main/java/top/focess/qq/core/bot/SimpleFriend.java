package top.focess.qq.core.bot;

import com.google.common.collect.Maps;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import top.focess.qq.api.bot.Bot;
import top.focess.qq.api.bot.Friend;

import java.util.Map;

public class SimpleFriend extends SimpleSpeaker implements Friend {

    private static final Map<Long, Map<Long,SimpleFriend>> FRIEND_MAP = Maps.newConcurrentMap();
    private final net.mamoe.mirai.contact.Friend nativeFriend;

    private SimpleFriend(Bot bot, net.mamoe.mirai.contact.Friend nativeFriend) {
        super(bot,nativeFriend);
        this.nativeFriend = nativeFriend;
    }

    @Nullable
    public static Friend get(Bot bot, @Nullable net.mamoe.mirai.contact.Friend nativeFriend) {
        if (nativeFriend == null)
            return null;
        if (bot.getId() != nativeFriend.getBot().getId())
            return null;
        Map<Long, SimpleFriend> map = FRIEND_MAP.computeIfAbsent(bot.getId(), k -> Maps.newConcurrentMap());
        return map.computeIfAbsent(nativeFriend.getId(), k -> new SimpleFriend(bot, nativeFriend));
    }

    public static void remove(Bot bot) {
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
        return nativeFriend.getAvatarUrl();
    }

    @Override
    public void delete() {
        this.nativeFriend.delete();
    }

}