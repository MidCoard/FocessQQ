package top.focess.qq.core.bot.mirai;

import com.google.common.collect.Maps;
import net.mamoe.mirai.Bot;
import net.mamoe.mirai.contact.ContactList;
import net.mamoe.mirai.contact.MemberPermission;
import net.mamoe.mirai.message.data.PlainText;
import net.mamoe.mirai.utils.ExternalResource;
import org.checkerframework.checker.nullness.qual.NonNull;
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
import top.focess.qq.api.bot.message.MessageChain;
import top.focess.qq.api.plugin.Plugin;
import top.focess.qq.core.bot.QQBot;
import top.focess.qq.core.bot.contact.*;
import top.focess.qq.core.bot.mirai.message.MiraiAudio;
import top.focess.qq.core.bot.mirai.message.MiraiImage;
import top.focess.qq.core.bot.mirai.message.MiraiMessage;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class MiraiBot extends QQBot {

    private final Map<Long,Friend> friendMap = Maps.newHashMap();
    private final Map<Long,Group> groupMap = Maps.newHashMap();
    private final Map<Long,Member> memberMap = Maps.newHashMap();
    private final Map<Long,Stranger> strangerMap = Maps.newHashMap();
    private final Map<Long,OtherClient> clientMap =   Maps.newHashMap();
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
        net.mamoe.mirai.contact.Friend friend = this.nativeBot.getFriendOrFail(id);
        return friendMap.computeIfAbsent(id, i -> new SimpleFriend(this,friend.getId(), friend.getRemark(), friend.getNick(), friend.getAvatarUrl()));
    }

    @Override
    public @NonNull Group getGroupOrFail(final long id) {
        net.mamoe.mirai.contact.Group group = this.nativeBot.getGroupOrFail(id);
        return groupMap.computeIfAbsent(id, i -> new SimpleGroup(this,group.getId(), group.getName(), group.getAvatarUrl()));
    }

    @Override
    public @Nullable Group getGroup(final long id) {
        net.mamoe.mirai.contact.Group group = this.nativeBot.getGroup(id);
        if (group == null)
            return null;
        return groupMap.computeIfAbsent(id, i -> new SimpleGroup(this,group.getId(), group.getName(), group.getAvatarUrl()));
    }

    @Override
    public @Nullable Friend getFriend(final long id) {
        net.mamoe.mirai.contact.Friend friend = this.nativeBot.getFriend(id);
        if (friend == null)
            return null;
        return friendMap.computeIfAbsent(id, i -> new SimpleFriend(this,friend.getId(), friend.getRemark(), friend.getNick(), friend.getAvatarUrl()));
    }

    @Override
    @NotNull
    @UnmodifiableView
    public List<Friend> getFriends() {
        return this.nativeBot.getFriends().stream().map(friend -> new SimpleFriend(this,friend.getId(),friend.getRemark(),friend.getNick(),friend.getAvatarUrl())).collect(Collectors.toUnmodifiableList());
    }

    @Override
    @NotNull
    @UnmodifiableView
    public List<Group> getGroups() {
        return this.nativeBot.getGroups().stream().map(group -> new SimpleGroup(this,group.getId(),group.getName(),group.getAvatarUrl())).collect(Collectors.toUnmodifiableList());
    }

    @Override
    public boolean isOnline() {
        return this.nativeBot.isOnline();
    }

    @Override
    @NotNull
    public Friend getAsFriend() {
        net.mamoe.mirai.contact.Friend friend = this.nativeBot.getAsFriend();
        return friendMap.computeIfAbsent(this.getId(), i -> new SimpleFriend(this,friend.getId(), friend.getRemark(), friend.getNick(), friend.getAvatarUrl()));

    }

    @Override
    public void sendMessage(Transmitter transmitter, Message message) {
        net.mamoe.mirai.message.data.Message mess = toMiraiMessage(message);
        if (transmitter instanceof Group) {
            net.mamoe.mirai.contact.Group group = this.nativeBot.getGroupOrFail(transmitter.getId());
            group.sendMessage(mess);
        } else if (transmitter instanceof Friend) {
            net.mamoe.mirai.contact.Friend friend = this.nativeBot.getFriendOrFail(transmitter.getId());
            friend.sendMessage(mess);
        } else if (transmitter instanceof Stranger) {
            net.mamoe.mirai.contact.Stranger stranger = this.nativeBot.getStrangerOrFail(transmitter.getId());
            stranger.sendMessage(mess);
        }
    }

    @Override
    public void sendMessage(Transmitter transmitter, String message) {
        this.sendMessage(transmitter, new MiraiMessage(new PlainText(message)));
    }

    @Override
    public @Nullable Image uploadImage(Transmitter transmitter, InputStream resource) {
        if (transmitter instanceof Group) {
            net.mamoe.mirai.contact.Group group = this.nativeBot.getGroupOrFail(transmitter.getId());
            try {
                return new MiraiImage(group.uploadImage(ExternalResource.create(resource)));
            } catch (IOException e) {
                return null;
            }
        } else if (transmitter instanceof Friend) {
            net.mamoe.mirai.contact.Friend friend = this.nativeBot.getFriendOrFail(transmitter.getId());
            try {
                return new MiraiImage(friend.uploadImage(ExternalResource.create(resource)));
            } catch (IOException e) {
                return null;
            }
        } else if (transmitter instanceof Stranger) {
            net.mamoe.mirai.contact.Stranger stranger = this.nativeBot.getStrangerOrFail(transmitter.getId());
            try {
                return new MiraiImage(stranger.uploadImage(ExternalResource.create(resource)));
            } catch (IOException e) {
                return null;
            }
        }
        return null;
    }

    @Override
    public @Nullable Audio uploadAudio(Speaker speaker, InputStream inputStream) {
        if (speaker instanceof Friend) {
            net.mamoe.mirai.contact.Friend friend = this.nativeBot.getFriendOrFail(speaker.getId());
            try {
                return new MiraiAudio(friend.uploadAudio(ExternalResource.create(inputStream)));
            } catch (IOException e) {
                return null;
            }
        } else if (speaker instanceof Group) {
            net.mamoe.mirai.contact.Group group = this.nativeBot.getGroupOrFail(speaker.getId());
            try {
                return new MiraiAudio(group.uploadAudio(ExternalResource.create(inputStream)));
            } catch (IOException e) {
                return null;
            }
        }
        return null;
    }

    @Override
    public void deleteFriend(Friend friend) {
        this.nativeBot.getFriendOrFail(friend.getId()).delete();
        this.friendMap.remove(friend.getId());
    }

    @Override
    public void quitGroup(Group group) {
        this.nativeBot.getGroupOrFail(group.getId()).quit();
        this.groupMap.remove(group.getId());
    }

    @Override
    public @Nullable Member getMember(Group group, long id) {
        net.mamoe.mirai.contact.Group nativeGroup = this.nativeBot.getGroupOrFail(group.getId());
        net.mamoe.mirai.contact.Member member = nativeGroup.get(id);
        if (member == null)
            return null;
        return memberMap.computeIfAbsent(id, i -> new SimpleMember(group,member.getId(),member.getRemark(),member.getNick(),member.getNameCard(), toCommandPermission(member.getPermission())));
    }

    @Override
    public Member getMemberOrFail(Group group, long id) {
        net.mamoe.mirai.contact.Group nativeGroup = this.nativeBot.getGroupOrFail(group.getId());
        net.mamoe.mirai.contact.Member member = nativeGroup.getOrFail(id);
        return memberMap.computeIfAbsent(id, i -> new SimpleMember(group,member.getId(),member.getRemark(),member.getNick(),member.getNameCard(), toCommandPermission(member.getPermission())));
    }

    @Override
    public Member getAsMember(Group group) {
        net.mamoe.mirai.contact.Group nativeGroup = this.nativeBot.getGroupOrFail(group.getId());
        net.mamoe.mirai.contact.Member member = nativeGroup.getBotAsMember();
        return memberMap.computeIfAbsent(member.getId(), i -> new SimpleMember(group,member.getId(),member.getRemark(),member.getNick(),member.getNameCard(), toCommandPermission(member.getPermission())));
    }

    @Override
    public List<Member> getMembers(Group group) {
        net.mamoe.mirai.contact.Group nativeGroup = this.nativeBot.getGroupOrFail(group.getId());
        return nativeGroup.getMembers().stream().map(member -> memberMap.computeIfAbsent(member.getId(), i -> new SimpleMember(group,member.getId(),member.getRemark(),member.getNick(),member.getNameCard(), toCommandPermission(member.getPermission())))).collect(Collectors.toList());
    }

    private static CommandPermission toCommandPermission(MemberPermission permission) {
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

    private static net.mamoe.mirai.message.data.Message toMiraiMessage(Message message) {
        if (message instanceof MiraiMessage)
            return ((MiraiMessage) message).getMessage();
        if (message instanceof MessageChain) {
            if (((MessageChain) message).isEmpty())
                throw new IllegalArgumentException("MessageChain is empty");
            Message first = ((MessageChain) message).get(0);
            if (!(first instanceof MiraiMessage))
                throw new IllegalArgumentException("MessageChain's first element is not a MiraiMessage");
            net.mamoe.mirai.message.data.Message ret = ((MiraiMessage) first).getMessage();
            for (int i = 1; i < ((MessageChain) message).size(); i++)
                if (!(((MessageChain) message).get(i) instanceof MiraiMessage))
                    throw new IllegalArgumentException("MessageChain's element is not a MiraiMessage");
                else
                    ret = ret.plus(((MiraiMessage) ((MessageChain) message).get(i)).getMessage());
            return ret;
        }
        throw new IllegalArgumentException("Unknown message type: " + message.getClass());
    }

    @Override
    public @Nullable Stranger getStranger(long id) {
        net.mamoe.mirai.contact.Stranger stranger = this.nativeBot.getStranger(id);
        if (stranger == null)
            return null;
        return strangerMap.computeIfAbsent(id, i -> new SimpleStranger(this,stranger.getId(), stranger.getRemark(), stranger.getNick()));
    }

    @Override
    public Stranger getStrangerOrFail(long id) {
        net.mamoe.mirai.contact.Stranger stranger = this.nativeBot.getStrangerOrFail(id);
        return strangerMap.computeIfAbsent(id, i -> new SimpleStranger(this,stranger.getId(), stranger.getRemark(), stranger.getNick()));
    }

    @Override
    public OtherClient getOtherClientOrFail(long id) {
        ContactList<net.mamoe.mirai.contact.OtherClient> clients = this.nativeBot.getOtherClients();
        for (net.mamoe.mirai.contact.OtherClient client : clients)
            if (client.getId() == id)
                return clientMap.computeIfAbsent(id, i -> new SimpleOtherClient(this,client.getId(), client.getInfo().getDeviceName(), client.getInfo().getDeviceKind(),client.getInfo().getAppId()));
       throw new NullPointerException("No such client");
    }

    @Override
    public @Nullable OtherClient getOtherClient(long id) {
        ContactList<net.mamoe.mirai.contact.OtherClient> clients = this.nativeBot.getOtherClients();
        for (net.mamoe.mirai.contact.OtherClient client : clients)
            if (client.getId() == id)
                return clientMap.computeIfAbsent(id, i -> new SimpleOtherClient(this,client.getId(), client.getInfo().getDeviceName(), client.getInfo().getDeviceKind(),client.getInfo().getAppId()));
        return null;
    }
}
