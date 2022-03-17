package top.focess.qq.core.bot;

import com.google.common.collect.Lists;
import net.mamoe.mirai.contact.Friend;
import net.mamoe.mirai.contact.Group;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import top.focess.qq.FocessQQ;
import top.focess.qq.api.bot.Bot;
import top.focess.qq.api.bot.BotLoginException;
import top.focess.qq.api.plugin.Plugin;

import java.util.List;
import java.util.Objects;

public class SimpleBot implements Bot {

    private final long username;
    private final String password;
    private final Plugin plugin;
    private net.mamoe.mirai.Bot nativeBot;

    public SimpleBot(long username, String password, net.mamoe.mirai.Bot bot, Plugin plugin) {
        this.username = username;
        this.password = password;
        this.nativeBot = bot;
        this.plugin = plugin;
    }

    @Override
    @NotNull
    public net.mamoe.mirai.Bot getNativeBot() {
        return this.nativeBot;
    }

    @Override
    public boolean relogin() throws BotLoginException {
        return FocessQQ.getBotManager().relogin(this);
    }

    @Override
    public boolean login() throws BotLoginException {
        return FocessQQ.getBotManager().login(this);
    }

    @Override
    public boolean logout() {
        return FocessQQ.getBotManager().logout(this);
    }

    @Override
    public @NotNull Friend getFriendOrFail(long id) {
        return this.nativeBot.getFriendOrFail(id);
    }

    @Override
    public @Nullable Group getGroup(long id) {
        return this.nativeBot.getGroup(id);
    }

    @Override
    public @Nullable Friend getFriend(long id) {
        return this.nativeBot.getFriend(id);
    }

    @Override
    public @NotNull List<Friend> getFriends() {
        return Lists.newArrayList(this.nativeBot.getFriends());
    }

    @Override
    public @NotNull List<Group> getGroups() {
        return Lists.newArrayList(this.nativeBot.getGroups());
    }

    @Override
    public boolean isOnline() {
        return this.nativeBot.isOnline();
    }

    @Override
    @NotNull
    public Friend getAsFriend() {
        return this.nativeBot.getAsFriend();
    }

    @Override
    public long getId() {
        return this.nativeBot.getId();
    }

    @Override
    public boolean isDefaultBot() {
        return FocessQQ.getBot().equals(this);
    }

    public long getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public void setNativeBot(net.mamoe.mirai.Bot nativeBot) {
        this.nativeBot = nativeBot;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SimpleBot simpleBot = (SimpleBot) o;

        return Objects.equals(nativeBot, simpleBot.nativeBot);
    }

    @Override
    public int hashCode() {
        return nativeBot != null ? nativeBot.hashCode() : 0;
    }

    public Plugin getPlugin() {
        return plugin;
    }
}
