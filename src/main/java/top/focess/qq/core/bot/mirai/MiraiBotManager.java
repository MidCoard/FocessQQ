package top.focess.qq.core.bot.mirai;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import kotlin.coroutines.Continuation;
import kotlin.coroutines.CoroutineContext;
import kotlin.coroutines.EmptyCoroutineContext;
import net.mamoe.mirai.BotFactory;
import net.mamoe.mirai.auth.BotAuthorization;
import net.mamoe.mirai.auth.QRCodeLoginListener;
import net.mamoe.mirai.contact.OtherClient;
import net.mamoe.mirai.event.Listener;
import net.mamoe.mirai.event.events.*;
import net.mamoe.mirai.utils.BotConfiguration;
import net.mamoe.mirai.utils.DeviceVerificationRequests;
import net.mamoe.mirai.utils.DeviceVerificationResult;
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
import top.focess.qq.api.bot.contact.*;
import top.focess.qq.api.event.EventManager;
import top.focess.qq.api.event.EventSubmitException;
import top.focess.qq.api.event.bot.BotReloginEvent;
import top.focess.qq.api.event.bot.*;
import top.focess.qq.api.event.chat.FriendChatEvent;
import top.focess.qq.api.event.chat.GroupChatEvent;
import top.focess.qq.api.event.chat.StrangerChatEvent;
import top.focess.qq.api.event.group.MemberCardNameChangeEvent;
import top.focess.qq.api.event.group.MemberCommandPermissionChangeEvent;
import top.focess.qq.api.event.recall.FriendRecallEvent;
import top.focess.qq.api.event.recall.GroupRecallEvent;
import top.focess.qq.api.event.request.FriendRequestEvent;
import top.focess.qq.api.event.request.GroupRequestEvent;
import top.focess.qq.api.plugin.Plugin;
import top.focess.qq.api.scheduler.Schedulers;
import top.focess.qq.api.util.IOHandler;
import top.focess.qq.core.bot.contact.SimpleMember;
import top.focess.qq.core.bot.mirai.message.MiraiMessage;
import top.focess.qq.core.bot.mirai.message.MiraiMessageChain;
import top.focess.qq.core.bot.mirai.message.MiraiMessageSource;
import top.focess.qq.core.permission.Permission;
import top.focess.qq.core.permission.PermissionEnv;
import top.focess.scheduler.Scheduler;

import javax.imageio.stream.FileImageOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.Future;

import static top.focess.qq.core.bot.mirai.MiraiBot.toCommandPermission;

@PermissionEnv(values = {Permission.REMOVE_BOT_MANAGER, Permission.BOT_LOGIN, Permission.BOT_LOGOUT, Permission.BOT_RELOGIN})
public class MiraiBotManager implements BotManager {

    private static final Scheduler SCHEDULER = Schedulers.newFocessScheduler(FocessQQ.getMainPlugin(), "BotManager");

    private static final Map<Bot, List<Listener<?>>> BOT_LISTENER_MAP = Maps.newHashMap();

    private static final Map<Plugin, List<Bot>> PLUGIN_BOT_MAP = Maps.newHashMap();

    private static final Map<Long, Bot> BOTS = Maps.newConcurrentMap();

    public void removeAll() {
        Permission.checkPermission(Permission.REMOVE_BOT_MANAGER);
        for (final Long id : BOTS.keySet())
            this.remove(id);
        //remove default bot
        if (FocessQQ.getBot() != null) {
            final Bot b = BOTS.remove(FocessQQ.getBot().getId());
            if (b != null)
                b.logout();
        }
        PLUGIN_BOT_MAP.clear();
    }

    public void remove(final Plugin plugin) {
        Permission.checkPermission(Permission.REMOVE_BOT_MANAGER);
        for (final Bot b : PLUGIN_BOT_MAP.getOrDefault(plugin, Lists.newArrayList()))
            this.remove(b.getId());
        PLUGIN_BOT_MAP.remove(plugin);
    }

    @Override
    @NotNull
    public Future<Bot> login(final long id, final String password, final Plugin plugin, final BotProtocol protocol) {
        Permission.checkPermission(Permission.BOT_LOGIN);
        return SCHEDULER.submit(() -> this.loginDirectly(id, password, plugin, protocol),"login-bot-" + id);
    }

    @Override
    @NotNull
    public Bot loginDirectly(final long id, final String password, final Plugin plugin,final BotProtocol botProtocol) throws BotLoginException {
        Permission.checkPermission(Permission.BOT_LOGIN);
        final net.mamoe.mirai.Bot bot = login0(id, password, botProtocol);
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
        Permission.checkPermission(Permission.BOT_LOGIN);
        this.checkBot(b);
        if (b.isOnline())
            return false;
        final long id = b.getId();
        final String password = ((MiraiBot) b).getPassword();
        final BotProtocol botProtocol = b.getBotProtocol();
        this.setup((MiraiBot) b, this.login0(id, password, botProtocol));
        return true;
    }

    private net.mamoe.mirai.Bot login0(long id, String password, BotProtocol botProtocol) throws BotLoginException {
        final BotConfiguration configuration = BotConfiguration.getDefault();
        configuration.setProtocol(getProtocol(botProtocol));
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
                    return IOHandler.getConsoleIoHandler().inputMessage().toString();
                } catch (final InputTimeoutException e) {
                    return null;
                }
            }

            @Nullable
            @Override
            public Object onSolveSliderCaptcha(@NotNull final net.mamoe.mirai.Bot bot, @NotNull final String s, @NotNull final Continuation<? super String> continuation) {
                FocessQQ.getLogger().info(s);
                try {
                    return IOHandler.getConsoleIoHandler().inputMessage().toString();
                } catch (final InputTimeoutException ignored) {
                    return null;
                }
            }

            @Nullable
            @Override
            public Object onSolveUnsafeDeviceLoginVerify(@NotNull final net.mamoe.mirai.Bot bot, @NotNull final String s, @NotNull final Continuation<? super String> continuation) {
                FocessQQ.getLogger().info(s);
                try {
                    return IOHandler.getConsoleIoHandler().inputMessage().toString();
                } catch (final InputTimeoutException ignored) {
                    return null;
                }
            }

            @Override
            public @NotNull Object onSolveDeviceVerification(@NotNull net.mamoe.mirai.Bot bot, @NotNull DeviceVerificationRequests requests, @NotNull Continuation<? super DeviceVerificationResult> continuation) {
                if (requests.getPreferSms()) {
                    FocessQQ.getLogger().info(requests.getSms().getCountryCode() + " " + requests.getSms().getPhoneNumber());
                    try {
                        IOHandler.getConsoleIoHandler().inputMessage();
                    } catch (final InputTimeoutException ignored) {
                    }

                    Object lock = new Object();
                    requests.getSms().requestSms(new Continuation<>() {
                        @Override
                        public @NotNull CoroutineContext getContext() {
                            return EmptyCoroutineContext.INSTANCE;
                        }

                        @Override
                        public void resumeWith(@NotNull Object result) {
                            synchronized (lock) {
                                lock.notify();
                            }
                        }
                    });
                    synchronized (lock) {
                        try {
                            lock.wait();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                    String code;
                    try {
                        code = IOHandler.getConsoleIoHandler().inputMessage().toString();
                    } catch (final InputTimeoutException ignored) {
                        code = "";
                    }
                    return requests.getSms().solved(code);
                } else {
                    FocessQQ.getLogger().info(requests.getFallback().getUrl());
                    try {
                        IOHandler.getConsoleIoHandler().inputMessage();
                    } catch (final InputTimeoutException ignored) {
                    }
                    return requests.getFallback().solved();
                }
            }

            @NotNull
            @Override
            public QRCodeLoginListener createQRCodeLoginListener(@NotNull net.mamoe.mirai.Bot bot) {
                return new QRCodeLoginListener() {
                    @Override
                    public void onStateChanged(@NotNull net.mamoe.mirai.Bot bot, @NotNull QRCodeLoginListener.State state) {
                        FocessQQ.getLogger().infoLang("bot-login-qrcode-state", id, state.name());
                    }

                    @Override
                    public void onFetchQRCode(@NotNull net.mamoe.mirai.Bot bot, @NotNull byte[] bytes) {
                        try {
                            final FileImageOutputStream outputStream = new FileImageOutputStream(new File("qrcode.jpg"));
                            outputStream.write(bytes);
                            outputStream.close();
                        } catch (final IOException e) {
                            FocessQQ.getLogger().thrLang("exception-load-qrcode-picture", e);
                        }
                    }
                };
            }
        });
        final net.mamoe.mirai.Bot bot = BotFactory.INSTANCE.newBot(id, password.equalsIgnoreCase("qr") ? BotAuthorization.byQRCode() : BotAuthorization.byPassword(password), configuration);
        try {
            bot.login();
        } catch (final Exception e) {
            bot.close();
            throw new BotLoginException(id, e);
        }
        return bot;
    }

    private void setup(final MiraiBot b, final net.mamoe.mirai.Bot bot) {
        b.setNativeBot(bot);
        try {
            EventManager.submit(new BotLoginEvent(b));
        } catch (final EventSubmitException e) {
            FocessQQ.getLogger().thrLang("exception-submit-bot-login-event", e);
        }
        final List<Listener<?>> listeners = Lists.newArrayList();
        listeners.add(bot.getEventChannel().subscribeAlways(GroupMessageEvent.class, event -> {
            final Group group = Objects.requireNonNull(b.getGroup(event.getGroup()));
            final GroupChatEvent e = new GroupChatEvent(b, Objects.requireNonNull(b.getMember(event.getSender())), new MiraiMessageChain(event.getMessage()), MiraiMessageSource.of(event.getSource()));
            try {
                EventManager.submit(e);
            } catch (final EventSubmitException eventSubmitException) {
                FocessQQ.getLogger().thrLang("exception-submit-group-chat-event", eventSubmitException);
            }
        }));
        listeners.add(bot.getEventChannel().subscribeAlways(FriendMessageEvent.class, event -> {
            final Friend friend = Objects.requireNonNull(b.getFriend(event.getSender()));
            final FriendChatEvent e = new FriendChatEvent(b, friend, new MiraiMessageChain(event.getMessage()), MiraiMessageSource.of(event.getSource()));
            try {
                EventManager.submit(e);
            } catch (final EventSubmitException eventSubmitException) {
                FocessQQ.getLogger().thrLang("exception-submit-friend-chat-event", eventSubmitException);
            }
        }));
        listeners.add(bot.getEventChannel().subscribeAlways(MessageRecallEvent.GroupRecall.class, event -> {
            final Group group = Objects.requireNonNull(b.getGroup(event.getGroup()));
            final GroupRecallEvent e = new GroupRecallEvent(b, Objects.requireNonNull(b.getMember(event.getAuthor())), event.getMessageIds(), b.getMember(event.getOperator()));
            try {
                EventManager.submit(e);
            } catch (final EventSubmitException ex) {
                FocessQQ.getLogger().thrLang("exception-submit-group-recall-event", ex);
            }
        }));
        listeners.add(bot.getEventChannel().subscribeAlways(MessageRecallEvent.FriendRecall.class, event -> {
            final Friend friend = Objects.requireNonNull(b.getFriend(event.getAuthor()));
            final FriendRecallEvent e = new FriendRecallEvent(b, friend, event.getMessageIds());
            try {
                EventManager.submit(e);
            } catch (final EventSubmitException ex) {
                FocessQQ.getLogger().thrLang("exception-submit-friend-recall-event", ex);
            }
        }));
        listeners.add(bot.getEventChannel().subscribeAlways(NewFriendRequestEvent.class, event -> {
            final Group group = b.getGroup(event.getFromGroup());
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
            final Friend friend = b.getFriend(event.getInvitor());
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
            final Friend friend = Objects.requireNonNull(b.getFriend(event.getFriend()));
            final FriendInputStatusEvent e = new FriendInputStatusEvent(b, friend, event.getInputting());
            try {
                EventManager.submit(e);
            } catch (final EventSubmitException ex) {
                FocessQQ.getLogger().thrLang("exception-submit-friend-input-status-event", ex);
            }
        }));
        listeners.add(bot.getEventChannel().subscribeAlways(StrangerMessageEvent.class, event -> {
            final Stranger stranger = Objects.requireNonNull(b.getStranger(event.getStranger()));
            final StrangerChatEvent e = new StrangerChatEvent(b, stranger, new MiraiMessageChain(event.getMessage()), MiraiMessageSource.of(event.getSource()));
            try {
                EventManager.submit(e);
            } catch (final EventSubmitException ex) {
                FocessQQ.getLogger().thrLang("exception-submit-stranger-chat-event", ex);
            }
        }));
        listeners.add(bot.getEventChannel().subscribeAlways(MessagePostSendEvent.class, event -> {
            final Contact contact = getContact(b, event.getTarget());
            final BotSendMessageEvent e = new BotSendMessageEvent(b, new MiraiMessageChain(event.getMessage()), contact);
            try {
                EventManager.submit(e);
            } catch (final EventSubmitException ex) {
                FocessQQ.getLogger().thrLang("exception-submit-bot-send-message-event", ex);
            }
        }));
        listeners.add(bot.getEventChannel().subscribeAlways(MessagePreSendEvent.class, event -> {
            final Contact contact = getContact(b, event.getTarget());
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
            final Contact contact = getContact(b, event.getSubject());
            final BotSendMessageEvent e = new BotSendMessageEvent(b, new MiraiMessageChain(event.getMessage()), contact);
            try {
                EventManager.submit(e);
            } catch (final EventSubmitException ex) {
                FocessQQ.getLogger().thrLang("exception-submit-bot-send-message-event", ex);
            }
        }));
        listeners.add(bot.getEventChannel().subscribeAlways(MemberPermissionChangeEvent.class, event -> {
            final Group group = Objects.requireNonNull(b.getGroup(event.getGroup()));
            final Member member = Objects.requireNonNull(group.getMember(event.getMember().getId()));
            ((SimpleMember) member).setPermission(toCommandPermission(event.getNew()));
            final MemberCommandPermissionChangeEvent e = new MemberCommandPermissionChangeEvent(member, toCommandPermission(event.getOrigin()), toCommandPermission(event.getNew()));
            try {
                EventManager.submit(e);
            } catch (final EventSubmitException ex) {
                FocessQQ.getLogger().thrLang("exception-submit-member-permission-change-event", ex);
            }
        }));
        listeners.add(bot.getEventChannel().subscribeAlways(MemberCardChangeEvent.class, event -> {
            final Group group = Objects.requireNonNull(b.getGroup(event.getGroup()));
            final Member member = Objects.requireNonNull(group.getMember(event.getMember().getId()));
            ((SimpleMember) member).setCardName(event.getNew());
            final MemberCardNameChangeEvent e = new MemberCardNameChangeEvent(member, event.getOrigin(), event.getNew());
            try {
                EventManager.submit(e);
            } catch (final EventSubmitException ex) {
                FocessQQ.getLogger().thrLang("exception-submit-member-card-change-event", ex);
            }
        }));
        BOT_LISTENER_MAP.put(b, listeners);
    }

    @Override
    public boolean logout(@NotNull final Bot bot) {
        Permission.checkPermission(Permission.BOT_LOGOUT);
        this.checkBot(bot);
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
        Permission.checkPermission(Permission.BOT_RELOGIN);
        this.checkBot(bot);
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
        Permission.checkPermission(Permission.REMOVE_BOT_MANAGER);
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

    private static BotConfiguration.MiraiProtocol getProtocol(final BotProtocol botProtocol) {
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

    @NotNull
    private static Contact getContact(final Bot bot, final net.mamoe.mirai.contact.Contact contact) {
        if (contact instanceof net.mamoe.mirai.contact.Group)
            return bot.getGroupOrFail(contact.getId());
        if (contact instanceof net.mamoe.mirai.contact.Friend)
            return bot.getFriendOrFail(contact.getId());
        if (contact instanceof net.mamoe.mirai.contact.Member)
            return bot.getGroupOrFail(((net.mamoe.mirai.contact.Member) contact).getGroup().getId()).getMemberOrFail(contact.getId());
        if (contact instanceof Stranger)
            return bot.getStrangerOrFail(contact.getId());
        if (contact instanceof OtherClient)
            return bot.getOtherClientOrFail(contact.getId());
        throw new NullPointerException();
    }
}
