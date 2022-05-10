package top.focess.qq.core.bot.mirai;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import kotlin.coroutines.Continuation;
import net.mamoe.mirai.BotFactory;
import net.mamoe.mirai.contact.Member;
import net.mamoe.mirai.contact.OtherClient;
import net.mamoe.mirai.event.Listener;
import net.mamoe.mirai.event.events.*;
import net.mamoe.mirai.utils.BotConfiguration;
import net.mamoe.mirai.utils.LoginSolver;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnmodifiableView;
import top.focess.command.InputTimeoutException;
import top.focess.qq.FocessQQ;
import top.focess.qq.api.bot.Bot;
import top.focess.qq.api.bot.BotLoginException;
import top.focess.qq.api.bot.BotManager;
import top.focess.qq.api.bot.BotProtocol;
import top.focess.qq.api.bot.contact.Contact;
import top.focess.qq.api.bot.contact.Friend;
import top.focess.qq.api.bot.contact.Group;
import top.focess.qq.api.bot.contact.Stranger;
import top.focess.qq.api.bot.message.MessageChain;
import top.focess.qq.api.event.EventManager;
import top.focess.qq.api.event.EventSubmitException;
import top.focess.qq.api.event.bot.BotReloginEvent;
import top.focess.qq.api.event.bot.*;
import top.focess.qq.api.event.chat.FriendChatEvent;
import top.focess.qq.api.event.chat.GroupChatEvent;
import top.focess.qq.api.event.chat.StrangerChatEvent;
import top.focess.qq.api.event.recall.FriendRecallEvent;
import top.focess.qq.api.event.recall.GroupRecallEvent;
import top.focess.qq.api.event.request.FriendRequestEvent;
import top.focess.qq.api.event.request.GroupRequestEvent;
import top.focess.qq.api.plugin.Plugin;
import top.focess.qq.api.schedule.Schedulers;
import top.focess.qq.api.util.IOHandler;
import top.focess.qq.core.bot.QQBot;
import top.focess.qq.core.bot.mirai.message.MiraiMessage;
import top.focess.qq.core.bot.mirai.message.MiraiMessageSource;
import top.focess.scheduler.Scheduler;

import javax.imageio.stream.FileImageOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.Future;

public class MiraiBotManager implements BotManager {

    private static final Scheduler SCHEDULER = Schedulers.newFocessScheduler(FocessQQ.getMainPlugin(), "BotManager");

    private static final Map<Bot, List<Listener<?>>> BOT_LISTENER_MAP = Maps.newHashMap();

    private static final Map<Plugin, List<Bot>> PLUGIN_BOT_MAP = Maps.newHashMap();

    private static final Map<Long, Bot> BOTS = Maps.newConcurrentMap();

    public void removeAll() {
        for (final Long id : BOTS.keySet())
            this.remove(id);
        //remove default bot
        final Bot b = BOTS.remove(FocessQQ.getBot().getId());
        if (b != null)
            b.logout();
    }

    public void remove(final Plugin plugin) {
        for (final Bot b : PLUGIN_BOT_MAP.getOrDefault(plugin, Lists.newArrayList()))
            this.remove(b.getId());
        PLUGIN_BOT_MAP.remove(plugin);
    }

    @Override
    @NotNull
    public Future<Bot> login(final long id, final String password, final Plugin plugin, final BotProtocol protocol) {
        return SCHEDULER.submit(() -> this.loginDirectly(id, password, plugin, protocol),"login-bot-" + id);
    }

    @Override
    @NotNull
    public Bot loginDirectly(final long id, final String password, final Plugin plugin,final BotProtocol botProtocol) throws BotLoginException {
        final BotConfiguration configuration = BotConfiguration.getDefault();
        configuration.setProtocol(getProtocol(botProtocol));
        final File cache = new File("devices/" + id + "/cache");
        if (!cache.exists())
            if (!cache.mkdirs())
                throw new BotLoginException(id, FocessQQ.getLangConfig().get("fatal-create-cache-dir-failed"));
        configuration.fileBasedDeviceInfo("devices/" + id + "/device.json");
        configuration.setCacheDir(cache);
        configuration.setLoginSolver(new LoginSolver() {
            @NotNull
            @Override
            public Object onSolvePicCaptcha(@NotNull final net.mamoe.mirai.Bot bot, final byte @NotNull [] bytes, @NotNull final Continuation<? super String> continuation) {
                try {
                    final FileImageOutputStream outputStream = new FileImageOutputStream(new File("captcha.jpg"));
                    outputStream.write(bytes);
                    outputStream.close();
                } catch (final IOException e) {
                    FocessQQ.getLogger().thrLang("exception-load-captcha-picture", e);
                }
                FocessQQ.getLogger().infoLang("input-captcha-code");
                try {
                    return IOHandler.getConsoleIoHandler().input();
                } catch (final InputTimeoutException e) {
                    return "";
                }
            }

            @Nullable
            @Override
            public Object onSolveSliderCaptcha(@NotNull final net.mamoe.mirai.Bot bot, @NotNull final String s, @NotNull final Continuation<? super String> continuation) {
                FocessQQ.getLogger().info(s);
                try {
                    IOHandler.getConsoleIoHandler().input();
                } catch (final InputTimeoutException ignored) {
                }
                return null;
            }

            @Nullable
            @Override
            public Object onSolveUnsafeDeviceLoginVerify(@NotNull final net.mamoe.mirai.Bot bot, @NotNull final String s, @NotNull final Continuation<? super String> continuation) {
                FocessQQ.getLogger().info(s);
                try {
                    IOHandler.getConsoleIoHandler().input();
                } catch (final InputTimeoutException ignored) {
                }
                return null;
            }
        });
        final net.mamoe.mirai.Bot bot;
        try {
            bot = BotFactory.INSTANCE.newBot(id, password, configuration);
            bot.login();
        } catch (final Exception e) {
            throw new BotLoginException(id, e);
        }
        final MiraiBot b = new MiraiBot(id, password, bot, botProtocol, plugin,this);
        this.setup(b, bot);
        PLUGIN_BOT_MAP.compute(plugin, (k, v) -> {
            if (v == null)
                v = Lists.newArrayList();
            v.add(b);
            return v;
        });
        BOTS.put(id, b);
        return b;
    }

    @Override
    public boolean login(final Bot b) throws BotLoginException {
        checkBot(b);
        if (b.isOnline())
            return false;
        final long id = b.getId();
        final BotConfiguration configuration = BotConfiguration.getDefault();
        configuration.setProtocol(getProtocol(((QQBot)b).getBotProtocol()));
        final File cache = new File("devices/" + id + "/cache");
        if (!cache.exists())
            if (!cache.mkdirs())
                throw new BotLoginException(id, FocessQQ.getLangConfig().get("fatal-create-cache-dir-failed"));
        configuration.fileBasedDeviceInfo("devices/" + id + "/device.json");
        configuration.setCacheDir(cache);
        configuration.setLoginSolver(new LoginSolver() {
            @Nullable
            @Override
            public Object onSolvePicCaptcha(@NotNull final net.mamoe.mirai.Bot bot, final byte @NotNull [] bytes, @NotNull final Continuation<? super String> continuation) {
                try {
                    final FileImageOutputStream outputStream = new FileImageOutputStream(new File("captcha.jpg"));
                    outputStream.write(bytes);
                    outputStream.close();
                } catch (final IOException e) {
                    FocessQQ.getLogger().thrLang("exception-load-captcha-picture", e);
                }
                FocessQQ.getLogger().infoLang("input-captcha-code");
                try {
                    return IOHandler.getConsoleIoHandler().input();
                } catch (final InputTimeoutException e) {
                    return null;
                }
            }

            @Nullable
            @Override
            public Object onSolveSliderCaptcha(@NotNull final net.mamoe.mirai.Bot bot, @NotNull final String s, @NotNull final Continuation<? super String> continuation) {
                FocessQQ.getLogger().info(s);
                try {
                    IOHandler.getConsoleIoHandler().input();
                } catch (final InputTimeoutException ignored) {
                }
                return null;
            }

            @Nullable
            @Override
            public Object onSolveUnsafeDeviceLoginVerify(@NotNull final net.mamoe.mirai.Bot bot, @NotNull final String s, @NotNull final Continuation<? super String> continuation) {
                FocessQQ.getLogger().info(s);
                try {
                    IOHandler.getConsoleIoHandler().input();
                } catch (final InputTimeoutException ignored) {
                }
                return null;
            }
        });
        final net.mamoe.mirai.Bot bot;
        try {
            bot = BotFactory.INSTANCE.newBot(id, ((MiraiBot) b).getPassword(), configuration);
            bot.login();
        } catch (final Exception e) {
            throw new BotLoginException(id, e);
        }
        this.setup((MiraiBot) b, bot);
        return true;
    }

    private void setup(MiraiBot b, net.mamoe.mirai.Bot bot) {
        b.setNativeBot(bot);
        try {
            EventManager.submit(new BotLoginEvent(b));
        } catch (final EventSubmitException e) {
            FocessQQ.getLogger().thrLang("exception-submit-bot-login-event", e);
        }
        final List<Listener<?>> listeners = Lists.newArrayList();
        listeners.add(bot.getEventChannel().subscribeAlways(GroupMessageEvent.class, event -> {
            Group group = Objects.requireNonNull(b.getGroup(event.getGroup().getId()));
            if (event.getMessage().size() == 0)
                return;
            MessageChain messageChain = new MessageChain(new MiraiMessage(event.getMessage().get(0)));
            for (int i = 1; i < event.getMessage().size(); i++)
                messageChain.plus(new MiraiMessage(event.getMessage().get(i)));
            final GroupChatEvent e = new GroupChatEvent(b, Objects.requireNonNull(group.getMember(event.getSender().getId())), messageChain, MiraiMessageSource.of(event.getSource()));
            try {
                EventManager.submit(e);
            } catch (final EventSubmitException eventSubmitException) {
                FocessQQ.getLogger().thrLang("exception-submit-group-chat-event", eventSubmitException);
            }
        }));
        listeners.add(bot.getEventChannel().subscribeAlways(FriendMessageEvent.class, event -> {
            Friend friend = Objects.requireNonNull(b.getFriend(event.getSender().getId()));
            if (event.getMessage().size() == 0)
                return;
            MessageChain messageChain = new MessageChain(new MiraiMessage(event.getMessage().get(0)));
            for (int i = 1; i < event.getMessage().size(); i++)
                messageChain.plus(new MiraiMessage(event.getMessage().get(i)));
            final FriendChatEvent e = new FriendChatEvent(b, friend, messageChain, MiraiMessageSource.of(event.getSource()));
            try {
                EventManager.submit(e);
            } catch (final EventSubmitException eventSubmitException) {
                FocessQQ.getLogger().thrLang("exception-submit-friend-chat-event", eventSubmitException);
            }
        }));
        listeners.add(bot.getEventChannel().subscribeAlways(MessageRecallEvent.GroupRecall.class, event -> {
            Group group = Objects.requireNonNull(b.getGroup(event.getGroup().getId()));
            final GroupRecallEvent e = new GroupRecallEvent(b, Objects.requireNonNull(group.getMember(event.getAuthor().getId())), event.getMessageIds(), event.getOperator() != null ? Objects.requireNonNull(group.getMember(event.getOperator().getId())) : null);
            try {
                EventManager.submit(e);
            } catch (final EventSubmitException ex) {
                FocessQQ.getLogger().thrLang("exception-submit-group-recall-event", ex);
            }
        }));
        listeners.add(bot.getEventChannel().subscribeAlways(MessageRecallEvent.FriendRecall.class, event -> {
            Friend friend = Objects.requireNonNull(b.getFriend(event.getAuthor().getId()));
            final FriendRecallEvent e = new FriendRecallEvent(b, friend, event.getMessageIds());
            try {
                EventManager.submit(e);
            } catch (final EventSubmitException ex) {
                FocessQQ.getLogger().thrLang("exception-submit-friend-recall-event", ex);
            }
        }));
        listeners.add(bot.getEventChannel().subscribeAlways(NewFriendRequestEvent.class, event -> {
            Group group = Objects.requireNonNull(b.getGroup(event.getFromGroupId()));
            final FriendRequestEvent e = new FriendRequestEvent(b, event.getFromId(), event.getFromNick(), group, event.getMessage());
            try {
                EventManager.submit(e);
            } catch (final EventSubmitException ex) {
                FocessQQ.getLogger().thrLang("exception-submit-friend-request-event", ex);
            }
            if (e.getAccept() != null)
                if (e.getAccept())
                    event.accept();
                else event.reject(e.isBlackList());
        }));
        listeners.add(bot.getEventChannel().subscribeAlways(BotInvitedJoinGroupRequestEvent.class, event -> {
            Friend friend = Objects.requireNonNull(b.getFriend(event.getInvitorId()));
            final GroupRequestEvent e = new GroupRequestEvent(b, event.getGroupId(), event.getGroupName(), friend);
            try {
                EventManager.submit(e);
            } catch (final EventSubmitException ex) {
                FocessQQ.getLogger().thrLang("exception-submit-group-request-event", ex);
            }
            if (e.getAccept() != null)
                if (e.getAccept())
                    event.accept();
                else event.ignore();
        }));
        listeners.add(bot.getEventChannel().subscribeAlways(FriendInputStatusChangedEvent.class, event -> {
            Friend friend = Objects.requireNonNull(b.getFriend(event.getFriend().getId()));
            final FriendInputStatusEvent e = new FriendInputStatusEvent(b, friend, event.getInputting());
            try {
                EventManager.submit(e);
            } catch (final EventSubmitException ex) {
                FocessQQ.getLogger().thrLang("exception-submit-friend-input-status-event", ex);
            }
        }));
        listeners.add(bot.getEventChannel().subscribeAlways(StrangerMessageEvent.class, event -> {
            Stranger stranger = Objects.requireNonNull(b.getStranger(event.getStranger().getId()));
            if (event.getMessage().size() == 0)
                return;
            MessageChain messageChain = new MessageChain(new MiraiMessage(event.getMessage().get(0)));
            for (int i = 1; i < event.getMessage().size(); i++)
                messageChain.plus(new MiraiMessage(event.getMessage().get(i)));
            final StrangerChatEvent e = new StrangerChatEvent(b, stranger, messageChain, MiraiMessageSource.of(event.getSource()));
            try {
                EventManager.submit(e);
            } catch (final EventSubmitException ex) {
                FocessQQ.getLogger().thrLang("exception-submit-stranger-chat-event", ex);
            }
        }));
        listeners.add(bot.getEventChannel().subscribeAlways(MessagePostSendEvent.class, event -> {
            Contact contact = Objects.requireNonNull(getContact(b, event.getTarget()));
            final BotSendMessageEvent e = new BotSendMessageEvent(b, new MiraiMessage(event.getMessage()), contact);
            try {
                EventManager.submit(e);
            } catch (final EventSubmitException ex) {
                FocessQQ.getLogger().thrLang("exception-submit-bot-send-message-event", ex);
            }
        }));
        listeners.add(bot.getEventChannel().subscribeAlways(MessagePreSendEvent.class, event -> {
            Contact contact = Objects.requireNonNull(getContact(b, event.getTarget()));
            final BotPreSendMessageEvent e = new BotPreSendMessageEvent(b, new MiraiMessage(event.getMessage()), contact);
            try {
                EventManager.submit(e);
                if (e.isNeedUpdate() && e.getMessage() instanceof MiraiMessage)
                    event.setMessage(((MiraiMessage) e.getMessage()).getMessage());
            } catch (final EventSubmitException ex) {
                FocessQQ.getLogger().thrLang("exception-submit-bot-pre-send-message-event", ex);
            }
        }));
        listeners.add(bot.getEventChannel().subscribeAlways(MessageSyncEvent.class, event -> {
            Contact contact = Objects.requireNonNull(getContact(b, event.getSubject()));
            final BotSendMessageEvent e = new BotSendMessageEvent(b, new MiraiMessage(event.getMessage()), contact);
            try {
                EventManager.submit(e);
            } catch (final EventSubmitException ex) {
                FocessQQ.getLogger().thrLang("exception-submit-bot-send-message-event", ex);
            }
        }));
        BOT_LISTENER_MAP.put(b, listeners);
    }

    @Override
    public boolean logout(@NotNull final Bot bot) {
        checkBot(bot);
        if (!bot.isOnline())
            return false;
        ((MiraiBot)bot).getNativeBot().close();
        for (final Listener<?> listener : BOT_LISTENER_MAP.getOrDefault(bot, Lists.newArrayList()))
            listener.complete();
        BOT_LISTENER_MAP.remove(bot);
        try {
            EventManager.submit(new BotLogoutEvent(bot));
        } catch (final EventSubmitException e) {
            FocessQQ.getLogger().thrLang("exception-submit-bot-logout-event", e);
        }
        return true;
    }

    @Override
    @Nullable
    public Bot getBot(final long username) {
        return BOTS.get(username);
    }

    @Override
    public boolean relogin(@NotNull final Bot bot) throws BotLoginException {
        checkBot(bot);
        final boolean ret = this.logout(bot) && this.login(bot);
        try {
            EventManager.submit(new BotReloginEvent(bot));
        } catch (final EventSubmitException e) {
            FocessQQ.getLogger().thrLang("exception-submit-bot-relogin-event", e);
        }
        return ret;
    }

    @Override
    @UnmodifiableView
    public List<Bot> getBots() {
        return Collections.unmodifiableList(Lists.newArrayList(BOTS.values()));
    }

    @Nullable
    @Override
    public Bot remove(final long id) {
        if (FocessQQ.getBot().getId() == id)
            return null;
        final Bot b = BOTS.remove(id);
        if (b != null)
            b.logout();
        return b;
    }

    private void checkBot(@NotNull final Bot bot) {
        if (!(bot instanceof MiraiBot))
            throw new IllegalArgumentException("Bot must be instanced of MiraiBot");
    }

    private static BotConfiguration.MiraiProtocol getProtocol(BotProtocol botProtocol) {
        switch (botProtocol) {
            case IPAD:
                return BotConfiguration.MiraiProtocol.IPAD;
            case MACOS:
                return BotConfiguration.MiraiProtocol.MACOS;
            case ANDROID_PAD:
                return BotConfiguration.MiraiProtocol.ANDROID_PAD;
            case ANDROID_PHONE:
                return BotConfiguration.MiraiProtocol.ANDROID_PHONE;
            case ANDROID_WATCH:
                return BotConfiguration.MiraiProtocol.ANDROID_WATCH;
        }
        throw new IllegalArgumentException("Unknown bot protocol: " + botProtocol);
    }

    private static @Nullable Contact getContact(Bot bot, net.mamoe.mirai.contact.Contact contact) {
        if (contact instanceof net.mamoe.mirai.contact.Group)
            bot.getGroupOrFail(contact.getId());
        else if (contact instanceof net.mamoe.mirai.contact.Friend)
            bot.getFriendOrFail(contact.getId());
        else if (contact instanceof Member)
            bot.getGroupOrFail(contact.getId()).getMember(contact.getId());
        else if (contact instanceof Stranger)
            bot.getStrangerOrFail(contact.getId());
        else if (contact instanceof OtherClient)
            bot.getOtherClientOrFail(contact.getId());
        return null;

    }
}
