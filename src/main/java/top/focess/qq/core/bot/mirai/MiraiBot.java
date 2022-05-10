package top.focess.qq.core.bot.mirai;

import net.mamoe.mirai.Bot;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.UnmodifiableView;
import top.focess.qq.api.bot.BotManager;
import top.focess.qq.api.bot.BotProtocol;
import top.focess.qq.api.bot.contact.Friend;
import top.focess.qq.api.bot.contact.Group;
import top.focess.qq.api.plugin.Plugin;
import top.focess.qq.core.bot.QQBot;
import top.focess.qq.core.bot.contact.SimpleFriend;
import top.focess.qq.core.bot.contact.SimpleGroup;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class MiraiBot extends QQBot {
    private Bot nativeBot;

    public MiraiBot(final long username, final String password, final Bot bot, BotProtocol botProtocol, final Plugin plugin, final BotManager botManager) {
        super(username, password, plugin, botProtocol, botManager);
        this.nativeBot = bot;
    }

    public Bot getNativeBot() {
        return this.nativeBot;
    }

    public void setNativeBot(final Bot nativeBot) {
        this.nativeBot = nativeBot;
    }


    @Override
    public @NonNull Friend getFriendOrFail(final long id) {
        return Objects.requireNonNull(SimpleFriend.get(this, this.nativeBot.getFriendOrFail(id)));
    }

    @Override
    public @NonNull Group getGroupOrFail(final long id) {
        return Objects.requireNonNull(SimpleGroup.get(this, this.nativeBot.getGroupOrFail(id)));
    }

    @Override
    public @Nullable Group getGroup(final long id) {
        return SimpleGroup.get(this, this.nativeBot.getGroup(id));
    }

    @Override
    public @Nullable Friend getFriend(final long id) {
        return SimpleFriend.get(this, this.nativeBot.getFriend(id));
    }

    @Override
    @NotNull
    @UnmodifiableView
    public List<Friend> getFriends() {
        return this.nativeBot.getFriends().stream().map(i -> Objects.requireNonNull(SimpleFriend.get(this, i))).collect(Collectors.toUnmodifiableList());
    }

    @Override
    @NotNull
    @UnmodifiableView
    public List<Group> getGroups() {
        return this.nativeBot.getGroups().stream().map(i -> Objects.requireNonNull(SimpleGroup.get(this, i))).collect(Collectors.toUnmodifiableList());
    }

    @Override
    public boolean isOnline() {
        return this.nativeBot.isOnline();
    }

    @Override
    @NotNull
    public Friend getAsFriend() {
        return Objects.requireNonNull(SimpleFriend.get(this, this.nativeBot.getAsFriend()));
    }

}
