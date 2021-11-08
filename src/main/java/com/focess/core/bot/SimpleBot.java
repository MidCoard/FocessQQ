package com.focess.core.bot;

import com.focess.Main;
import com.focess.api.bot.Bot;
import com.google.common.collect.Lists;
import net.mamoe.mirai.contact.Friend;
import net.mamoe.mirai.contact.Group;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class SimpleBot implements Bot {

    private final long username;
    private final String password;
    private final net.mamoe.mirai.Bot nativeBot;

    public SimpleBot(long username, String password, net.mamoe.mirai.Bot bot) {
        this.username = username;
        this.password = password;
        this.nativeBot = bot;
    }

    @Override
    public net.mamoe.mirai.@NotNull Bot getNativeBot() {
        return this.nativeBot;
    }

    @Override
    public boolean relogin() {
        return Main.getBotManager().relogin(this);
    }

    @Override
    public boolean login() {
        return Main.getBotManager().login(this);
    }

    @Override
    public boolean logout() {
        return Main.getBotManager().logout(this);
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
        return false;
    }

    @Override
    @NotNull
    public Friend getAsFriend() {
        return this.nativeBot.getAsFriend();
    }

    public long getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }
}
