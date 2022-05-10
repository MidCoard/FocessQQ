package top.focess.qq.test.environment;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.jetbrains.annotations.UnmodifiableView;
import top.focess.qq.FocessQQ;
import top.focess.qq.api.bot.BotLoginException;
import top.focess.qq.api.bot.BotProtocol;
import top.focess.qq.api.bot.contact.*;
import top.focess.qq.api.bot.message.Audio;
import top.focess.qq.api.bot.message.Image;
import top.focess.qq.api.bot.message.Message;
import top.focess.qq.api.event.EventManager;
import top.focess.qq.api.event.EventSubmitException;
import top.focess.qq.api.event.bot.BotLoginEvent;
import top.focess.qq.api.event.bot.BotLogoutEvent;
import top.focess.qq.api.event.bot.BotReloginEvent;
import top.focess.qq.api.plugin.Plugin;
import top.focess.qq.core.bot.QQBot;
import top.focess.qq.core.bot.contact.SimpleFriend;

import java.io.InputStream;
import java.util.*;

public class TestBot extends QQBot {

    private final Set<Friend> friends = Sets.newConcurrentHashSet();
    private final Set<Group> groups = Sets.newConcurrentHashSet();

    private boolean isOnline = false;

    public TestBot(long id, String password, Plugin plugin, BotProtocol botProtocol, TestBotManager botManager) {
        super(id, password, plugin,botProtocol, botManager);
        Random random = new Random();
        int friendsSize = random.nextInt(50) + 1;
        for (int i = 0; i < friendsSize; i++) {
            String name = UUID.randomUUID().toString().substring(0, 8);
            this.friends.add(new SimpleFriend(this, random.nextLong(), name, name, "avatarUrl/" + name));
        }
        int groupsSize = random.nextInt(20) + 1;
        for (int i = 0; i < groupsSize; i++)
            this.groups.add(new TestGroup(random.nextLong(),this));
    }

    @Override
    public boolean relogin() throws BotLoginException {
        boolean ret = this.logout() && this.login();
        try {
            EventManager.submit(new BotReloginEvent(this));
        } catch (final EventSubmitException e) {
            FocessQQ.getLogger().thrLang("exception-submit-bot-relogin-event", e);
        }
        return ret;
    }

    @Override
    public boolean login() {
        if (this.isOnline)
            return false;
        this.isOnline = true;
        try {
            EventManager.submit(new BotLoginEvent(this));
        } catch (final EventSubmitException e) {
            FocessQQ.getLogger().thrLang("exception-submit-bot-login-event", e);
        }
        return this.isOnline;
    }

    @Override
    public boolean logout() {
        if (!this.isOnline)
            return false;
        this.isOnline = false;
        try {
            EventManager.submit(new BotLogoutEvent(this));
        } catch (final EventSubmitException e) {
            FocessQQ.getLogger().thrLang("exception-submit-bot-logout-event", e);
        }
        return this.isOnline;
    }

    @Override
    public @NonNull Friend getFriendOrFail(long id) {
        for (Friend friend : friends)
            if (friend.getId() == id)
                return friend;
        throw new NullPointerException();
    }

    @Override
    public @NonNull Group getGroupOrFail(long id) {
        for (Group group: groups)
            if (group.getId() == id)
                return group;
        throw new NullPointerException();
    }

    @Override
    public @Nullable Group getGroup(long id) {
        for (Group group: groups)
            if (group.getId() == id)
                return group;
        return null;
    }

    @Override
    public @Nullable Friend getFriend(long id) {
        for (Friend friend : friends)
            if (friend.getId() == id)
                return friend;
        return null;
    }

    @Override
    public @NonNull @UnmodifiableView List<Friend> getFriends() {
        return Collections.unmodifiableList(Lists.newArrayList(this.friends));
    }

    @Override
    public @NonNull @UnmodifiableView List<Group> getGroups() {
        return Collections.unmodifiableList(Lists.newArrayList(this.groups));
    }

    @Override
    public boolean isOnline() {
        return this.isOnline;
    }

    @Override
    public @NonNull Friend getAsFriend() {
        return new SimpleFriend(this,this.getId(),this.getId() + "",this.getId() + "","avatarUrl/" + this.getId());
    }

    @Override
    public void sendMessage(Transmitter transmitter, Message message) {
        System.out.println(this.getId() + " send message " + transmitter.getName() + "(" + transmitter.getId() + "): " + message);
    }

    @Override
    public void sendMessage(Transmitter transmitter, String message) {
        System.out.println(this.getId() + " send message " + transmitter.getName() + "(" + transmitter.getId() + "): " + message);
    }

    @Override
    public Image uploadImage(Transmitter transmitter, InputStream resource) {
        throw new UnsupportedOperationException();
    }

    @Override
    public @Nullable Audio uploadAudio(Speaker speaker, InputStream inputStream) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void deleteFriend(Friend friend) {
        this.removeFriend(friend);
    }

    @Override
    public void quitGroup(Group group) {
        this.removeGroup(group);
    }

    @Override
    public @Nullable Member getMember(Group group, long id) {
        return group.getMember(id);
    }

    @Override
    public Member getMemberOrFail(Group group, long id) {
        return group.getMemberOrFail(id);
    }

    @Override
    public Member getAsMember(Group group) {
        return group.getAsMember();
    }

    @Override
    public List<Member> getMembers(Group group) {
        return group.getMembers();
    }

    @Override
    public @Nullable Stranger getStranger(long id) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Stranger getStrangerOrFail(long id) {
        throw new UnsupportedOperationException();
    }

    @Override
    public OtherClient getOtherClientOrFail(long id) {
        throw new UnsupportedOperationException();
    }

    @Override
    public @Nullable OtherClient getOtherClient(long id) {
        throw new UnsupportedOperationException();
    }

    public void removeGroup(Group group) {
        this.groups.remove(group);
    }

    public void removeFriend(Friend friend) {
        this.friends.remove(friend);
    }
}
