package top.focess.qq.test.environment;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.jetbrains.annotations.UnmodifiableView;
import top.focess.qq.FocessQQ;
import top.focess.qq.api.bot.Bot;
import top.focess.qq.api.bot.BotLoginException;
import top.focess.qq.api.bot.contact.Friend;
import top.focess.qq.api.bot.contact.Group;
import top.focess.qq.api.event.EventManager;
import top.focess.qq.api.event.EventSubmitException;
import top.focess.qq.api.event.bot.BotLoginEvent;
import top.focess.qq.api.event.bot.BotLogoutEvent;
import top.focess.qq.api.event.bot.BotReloginEvent;
import top.focess.qq.api.plugin.Plugin;

import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.Set;

public class TestBot implements Bot {


    private final long id;
    private final String password;
    private final Plugin plugin;
    private final Set<Friend> friends = Sets.newConcurrentHashSet();
    private final Set<Group> groups = Sets.newConcurrentHashSet();

    private boolean isOnline = false;

    public TestBot(long id, String password, Plugin plugin) {
        this.id = id;
        this.password = password;
        this.plugin = plugin;
        Random random = new Random();
        int friendsSize = random.nextInt(50) + 1;
        for (int i = 0; i < friendsSize; i++)
            this.friends.add(new TestFriend(random.nextLong(),this));
        int groupsSize = random.nextInt(20) + 1;
        for (int i = 0; i < groupsSize; i++)
            this.groups.add(new TestGroup(random.nextLong(),this));
    }

    @Override
    public net.mamoe.mirai.Bot getNativeBot() {
        return null;
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
        return new TestFriend(this.id,this);
    }

    @Override
    public long getId() {
        return this.id;
    }

    @Override
    public boolean isDefaultBot() {
        return this == FocessQQ.getBot();
    }

    @Override
    public Plugin getPlugin() {
        return this.plugin;
    }

    @Override
    public boolean isAdministrator() {
        return FocessQQ.hasAdministratorId() && this.getId() == FocessQQ.getAdministratorId();
    }

    public void removeGroup(Group group) {
        this.groups.remove(group);
    }

    public void removeFriend(Friend friend) {
        this.friends.remove(friend);
    }
}
