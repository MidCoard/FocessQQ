package top.focess.qq.core.bot.mirai;

import com.google.common.collect.Maps;
import net.mamoe.mirai.Bot;
import net.mamoe.mirai.contact.ContactList;
import net.mamoe.mirai.contact.MemberPermission;
import net.mamoe.mirai.utils.ExternalResource;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.UnmodifiableView;
import top.focess.command.CommandPermission;
import top.focess.qq.api.bot.BotManager;
import top.focess.qq.api.bot.BotProtocol;
import top.focess.qq.api.bot.contact.*;
import top.focess.qq.api.bot.message.Audio;
import top.focess.qq.api.bot.message.Image;
import top.focess.qq.api.bot.message.Message;
import top.focess.qq.api.plugin.Plugin;
import top.focess.qq.core.bot.QQBot;
import top.focess.qq.core.bot.contact.*;
import top.focess.qq.core.bot.mirai.message.MiraiAudio;
import top.focess.qq.core.bot.mirai.message.MiraiImage;
import top.focess.qq.core.bot.mirai.message.MiraiMessageUtil;
import top.focess.util.Pair;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

public class MiraiBot extends QQBot {

    private final Map<Long, Friend> friendMap = Maps.newHashMap();
    private final Map<Long, Group> groupMap = Maps.newHashMap();
    private final Map<Pair<Long,Long>, Member> memberMap = Maps.newHashMap();
    private final Map<Long, Stranger> strangerMap = Maps.newHashMap();
    private final Map<Long, OtherClient> clientMap = Maps.newHashMap();
    private Bot nativeBot;

    public MiraiBot(final long username, final String password, final Bot bot, final BotProtocol botProtocol, final Plugin plugin, final BotManager botManager) {
        super(username, password, plugin, botProtocol, botManager);
        this.nativeBot = bot;
    }

    public Bot getNativeBot() {
        return this.nativeBot;
    }

    public void setNativeBot(final Bot nativeBot) {
        this.nativeBot = nativeBot;
    }

    public @Nullable Group getGroup(net.mamoe.mirai.contact.@Nullable Group group) {
        if (group == null || group.getBot().getId() != this.getId())
            return null;
        return this.groupMap.computeIfAbsent(group.getId(), i -> new SimpleGroup(this, group.getId(), group.getName(), group.getAvatarUrl()));
    }

    @Override
    public @Nullable Group getGroup(final long id) {
        if (groupMap.containsKey(id))
            return groupMap.get(id);
        return this.getGroup(this.nativeBot.getGroup(id));
    }

    public @Nullable Friend getFriend(net.mamoe.mirai.contact.@Nullable Friend friend) {
        if (friend == null || friend.getBot().getId() != this.getId())
            return null;
        return this.friendMap.computeIfAbsent(friend.getId(), i -> new SimpleFriend(this, friend.getId(), friend.getRemark(), friend.getNick(), friend.getAvatarUrl()));
    }

    @Override
    public @Nullable Friend getFriend(final long id) {
        if (friendMap.containsKey(id))
            return friendMap.get(id);
        return this.getFriend(this.nativeBot.getFriend(id));
    }

    @Override
    @NotNull
    @UnmodifiableView
    public List<Friend> getFriends() {
        return this.nativeBot.getFriends().stream().map(this::getFriend).filter(Objects::nonNull).collect(Collectors.toUnmodifiableList());
    }

    @Override
    @NotNull
    @UnmodifiableView
    public List<Group> getGroups() {
        return this.nativeBot.getGroups().stream().map(this::getGroup).filter(Objects::nonNull).collect(Collectors.toUnmodifiableList());
    }

    @Override
    public boolean isOnline() {
        return this.nativeBot.isOnline();
    }

    @Override
    @NotNull
    public Friend getAsFriend() {
        if (this.friendMap.containsKey(this.getId()))
            return this.friendMap.get(this.getId());
        return Objects.requireNonNull(this.getFriend(this.nativeBot.getAsFriend()));
    }

    @Override
    public void sendMessage(final Transmitter transmitter, final Message message) {
        final net.mamoe.mirai.message.data.Message mess = MiraiMessageUtil.toNativeMessage(message);
        if (mess == null)
            return;
        if (transmitter instanceof Group) {
            final net.mamoe.mirai.contact.Group group = this.nativeBot.getGroupOrFail(transmitter.getId());
            group.sendMessage(mess);
        } else if (transmitter instanceof Friend) {
            final net.mamoe.mirai.contact.Friend friend = this.nativeBot.getFriendOrFail(transmitter.getId());
            friend.sendMessage(mess);
        } else if (transmitter instanceof Stranger) {
            final net.mamoe.mirai.contact.Stranger stranger = this.nativeBot.getStrangerOrFail(transmitter.getId());
            stranger.sendMessage(mess);
        }
    }

    @Override
    public @Nullable Image uploadImage(final Transmitter transmitter, final InputStream resource) {
        try (ExternalResource externalResource = ExternalResource.create(resource)) {
            MiraiImage ret = null;
            if (transmitter instanceof Group) {
                final net.mamoe.mirai.contact.Group group = this.nativeBot.getGroupOrFail(transmitter.getId());
                ret = new MiraiImage(group.uploadImage(externalResource));
            } else if (transmitter instanceof Friend) {
                final net.mamoe.mirai.contact.Friend friend = this.nativeBot.getFriendOrFail(transmitter.getId());
                ret = new MiraiImage(friend.uploadImage(externalResource));
            } else if (transmitter instanceof Stranger) {
                final net.mamoe.mirai.contact.Stranger stranger = this.nativeBot.getStrangerOrFail(transmitter.getId());
                ret = new MiraiImage(stranger.uploadImage(externalResource));
            }
            return ret;
        } catch (IOException e) {
            return null;
        }
    }

    @Override
    public @Nullable Audio uploadAudio(final Speaker speaker, final InputStream inputStream) {
        try (ExternalResource externalResource = ExternalResource.create(inputStream)) {
            MiraiAudio ret = null;
            if (speaker instanceof Friend) {
                final net.mamoe.mirai.contact.Friend friend = this.nativeBot.getFriendOrFail(speaker.getId());
                ret = new MiraiAudio(friend.uploadAudio(externalResource));
            } else if (speaker instanceof Group) {
                final net.mamoe.mirai.contact.Group group = this.nativeBot.getGroupOrFail(speaker.getId());
                ret = new MiraiAudio(group.uploadAudio(externalResource));
            }
            return ret;
        } catch (IOException e) {
            return null;
        }
    }

    @Override
    public void deleteFriend(final @NotNull Friend friend) {
        this.nativeBot.getFriendOrFail(friend.getId()).delete();
        this.friendMap.remove(friend.getId());
    }

    @Override
    public void quitGroup(final @NotNull Group group) {
        this.nativeBot.getGroupOrFail(group.getId()).quit();
        this.groupMap.remove(group.getId());
    }

    @Override
    public @Nullable Member getMember(final Group group, final long id) {
        if (this.memberMap.containsKey(Pair.of(group.getId(), id)))
            return this.memberMap.get(Pair.of(group.getId(), id));
        final net.mamoe.mirai.contact.Group nativeGroup = this.nativeBot.getGroupOrFail(group.getId());
        return this.getMember(nativeGroup.get(id));
    }

    @Override
    public @Nullable Member getAsMember(final @NotNull Group group) {
        if (this.memberMap.containsKey(Pair.of(group.getId(), this.getId())))
            return this.memberMap.get(Pair.of(group.getId(), this.getId()));
        final net.mamoe.mirai.contact.Group nativeGroup = this.nativeBot.getGroupOrFail(group.getId());
        return this.getMember(nativeGroup.getBotAsMember());
    }

    @Override
    @UnmodifiableView
    public List<Member> getMembers(final @NotNull Group group) {
        final net.mamoe.mirai.contact.Group nativeGroup = this.nativeBot.getGroupOrFail(group.getId());
        return nativeGroup.getMembers().stream().map(this::getMember).filter(Objects::nonNull).collect(Collectors.toUnmodifiableList());
    }

    static CommandPermission toCommandPermission(final MemberPermission permission) {
        switch (permission) {
            case OWNER:
                return CommandPermission.OWNER;
            case ADMINISTRATOR:
                return CommandPermission.ADMINISTRATOR;
            case MEMBER:
                return CommandPermission.MEMBER;
        }
        throw new IllegalArgumentException("Unknown permission: " + permission);
    }

    @Override
    public @Nullable Stranger getStranger(final long id) {
        if (this.strangerMap.containsKey(id))
            return this.strangerMap.get(id);
        return this.getStranger(this.nativeBot.getStranger(id));
    }

    @Override
    public @Nullable OtherClient getOtherClient(final long id) {
        if (this.clientMap.containsKey(id))
            return this.clientMap.get(id);
        final ContactList<net.mamoe.mirai.contact.OtherClient> clients = this.nativeBot.getOtherClients();
        for (final net.mamoe.mirai.contact.OtherClient client : clients)
            if (client.getId() == id)
                return this.clientMap.computeIfAbsent(id, i -> new SimpleOtherClient(this, client.getId(), client.getInfo().getDeviceName(), client.getInfo().getDeviceKind(), client.getInfo().getAppId()));
        return null;
    }

    @Nullable
    public Member getMember(net.mamoe.mirai.contact.@Nullable Member sender) {
        if (sender == null || sender.getBot().getId() != this.getId())
            return null;
        Group group = this.getGroup(sender.getGroup());
        if (group == null)
            return null;
        return this.memberMap.computeIfAbsent(Pair.of(group.getId(), sender.getId()), i -> new SimpleMember(group, sender.getId(), sender.getRemark(), sender.getNick(), sender.getNameCard(), toCommandPermission(sender.getPermission())));
    }

    public @Nullable Stranger getStranger(net.mamoe.mirai.contact.@Nullable Stranger stranger) {
        if (stranger == null || stranger.getBot().getId() != this.getId())
            return null;
        return this.strangerMap.computeIfAbsent(stranger.getId(), i -> new SimpleStranger(this, stranger.getId(), stranger.getRemark(), stranger.getNick()));
    }
}
