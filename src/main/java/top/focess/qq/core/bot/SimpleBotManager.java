package top.focess.qq.core.bot;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import kotlin.coroutines.Continuation;
import net.mamoe.mirai.BotFactory;
import net.mamoe.mirai.event.Listener;
import net.mamoe.mirai.event.events.*;
import net.mamoe.mirai.utils.BotConfiguration;
import net.mamoe.mirai.utils.LoginSolver;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import top.focess.qq.FocessQQ;
import top.focess.qq.api.bot.Bot;
import top.focess.qq.api.bot.BotLoginException;
import top.focess.qq.api.bot.BotManager;
import top.focess.qq.api.bot.message.Message;
import top.focess.qq.api.bot.message.MessageChain;
import top.focess.qq.api.bot.message.MessageSource;
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
import top.focess.qq.api.schedule.Scheduler;
import top.focess.qq.api.schedule.Schedulers;
import top.focess.qq.api.util.IOHandler;
import top.focess.qq.api.util.InputTimeoutException;
import top.focess.qq.core.bot.contact.*;

import javax.imageio.stream.FileImageOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.Future;

public class SimpleBotManager implements BotManager {

    private static final Scheduler SCHEDULER = Schedulers.newFocessScheduler(FocessQQ.getMainPlugin(), "BotManager");

    private static final Map<Bot, List<Listener<?>>> BOT_LISTENER_MAP = Maps.newHashMap();

    private static final Map<Plugin, List<Bot>> PLUGIN_BOT_MAP = Maps.newHashMap();

    private static final Map<Long, Bot> BOTS = Maps.newConcurrentMap();

    public static void removeAll() {
        for (final Long id : BOTS.keySet())
            FocessQQ.getBotManager().remove(id);
        //remove default bot
        final Bot b = BOTS.remove(FocessQQ.getBot().getId());
        if (b != null)
            b.logout();
    }

    public static void remove(final Plugin plugin) {
        for (final Bot b : PLUGIN_BOT_MAP.getOrDefault(plugin, Lists.newArrayList()))
            FocessQQ.getBotManager().remove(b.getId());
        PLUGIN_BOT_MAP.remove(plugin);
    }

    @Override
    @NotNull
    public Future<Bot> login(final long id, final String password, final Plugin plugin) {
        return SCHEDULER.submit(() -> this.loginDirectly(id, password, plugin));
    }

    @Override
    @NotNull
    public Bot loginDirectly(final long id, final String password, final Plugin plugin) throws BotLoginException {
        final BotConfiguration configuration = BotConfiguration.getDefault();
        configuration.setProtocol(BotConfiguration.MiraiProtocol.ANDROID_PAD);
        final File cache = new File("devices/" + id + "/cache");
        if (!cache.exists())
            if (!cache.mkdirs())
                throw new BotLoginException(id, FocessQQ.getLangConfig().get("fatal-create-cache-dir-failed"));
        configuration.fileBasedDeviceInfo("devices/" + id + "/device.json");
        configuration.setCacheDir(cache);
        configuration.setLoginSolver(new LoginSolver() {
            @NotNull
            @Override
            public Object onSolvePicCaptcha(@NotNull final net.mamoe.mirai.Bot bot, @NotNull final byte[] bytes, @NotNull final Continuation<? super String> continuation) {
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
        final Bot b = new SimpleBot(id, password, bot, plugin);
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
        if (!b.isOnline() && b instanceof SimpleBot) {
            final long id = b.getId();
            final BotConfiguration configuration = BotConfiguration.getDefault();
            configuration.setProtocol(BotConfiguration.MiraiProtocol.ANDROID_PAD);
            final File cache = new File("devices/" + id + "/cache");
            if (!cache.exists())
                if (!cache.mkdirs())
                    throw new BotLoginException(id, FocessQQ.getLangConfig().get("fatal-create-cache-dir-failed"));
            configuration.fileBasedDeviceInfo("devices/" + id + "/device.json");
            configuration.setCacheDir(cache);
            configuration.setLoginSolver(new LoginSolver() {
                @Nullable
                @Override
                public Object onSolvePicCaptcha(@NotNull final net.mamoe.mirai.Bot bot, @NotNull final byte[] bytes, @NotNull final Continuation<? super String> continuation) {
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
                bot = BotFactory.INSTANCE.newBot(id, ((SimpleBot) b).getPassword(), configuration);
                bot.login();
                ((SimpleBot) b).setNativeBot(bot);
            } catch (final Exception e) {
                throw new BotLoginException(id, e);
            }
            this.setup(b, bot);
            return true;
        }
        return false;
    }

    private void setup(Bot b, net.mamoe.mirai.Bot bot) {
        try {
            EventManager.submit(new BotLoginEvent(b));
        } catch (final EventSubmitException e) {
            FocessQQ.getLogger().thrLang("exception-submit-bot-login-event", e);
        }
        final List<Listener<?>> listeners = Lists.newArrayList();
        listeners.add(bot.getEventChannel().subscribeAlways(GroupMessageEvent.class, event -> {
            final GroupChatEvent e = new GroupChatEvent(b, Objects.requireNonNull(SimpleMember.get(b, event.getSender())), MessageChain.of(event.getMessage()), MessageSource.of(event.getSource()));
            try {
                EventManager.submit(e);
            } catch (final EventSubmitException eventSubmitException) {
                FocessQQ.getLogger().thrLang("exception-submit-group-chat-event", eventSubmitException);
            }
        }));
        listeners.add(bot.getEventChannel().subscribeAlways(FriendMessageEvent.class, event -> {
            final FriendChatEvent e = new FriendChatEvent(b, Objects.requireNonNull(SimpleFriend.get(b, event.getFriend())), MessageChain.of(event.getMessage()), MessageSource.of(event.getSource()));
            try {
                EventManager.submit(e);
            } catch (final EventSubmitException eventSubmitException) {
                FocessQQ.getLogger().thrLang("exception-submit-friend-chat-event", eventSubmitException);
            }
        }));
        listeners.add(bot.getEventChannel().subscribeAlways(MessageRecallEvent.GroupRecall.class, event -> {
            final GroupRecallEvent e = new GroupRecallEvent(b, Objects.requireNonNull(SimpleMember.get(b, event.getAuthor())), event.getMessageIds(), SimpleMember.get(b, event.getOperator()));
            try {
                EventManager.submit(e);
            } catch (final EventSubmitException ex) {
                FocessQQ.getLogger().thrLang("exception-submit-group-recall-event", ex);
            }
        }));
        listeners.add(bot.getEventChannel().subscribeAlways(MessageRecallEvent.FriendRecall.class, event -> {
            final FriendRecallEvent e = new FriendRecallEvent(b, Objects.requireNonNull(SimpleFriend.get(b, event.getAuthor())), event.getMessageIds());
            try {
                EventManager.submit(e);
            } catch (final EventSubmitException ex) {
                FocessQQ.getLogger().thrLang("exception-submit-friend-recall-event", ex);
            }
        }));
        listeners.add(bot.getEventChannel().subscribeAlways(NewFriendRequestEvent.class, event -> {
            final FriendRequestEvent e = new FriendRequestEvent(b, event.getFromId(), event.getFromNick(), SimpleGroup.get(b, event.getFromGroup()), event.getMessage());
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
            final GroupRequestEvent e = new GroupRequestEvent(b, event.getGroupId(), event.getGroupName(), SimpleFriend.get(b, event.getInvitor()));
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
            final FriendInputStatusEvent e = new FriendInputStatusEvent(b, Objects.requireNonNull(SimpleFriend.get(b, event.getFriend())), event.getInputting());
            try {
                EventManager.submit(e);
            } catch (final EventSubmitException ex) {
                FocessQQ.getLogger().thrLang("exception-submit-friend-input-status-event", ex);
            }
        }));
        listeners.add(bot.getEventChannel().subscribeAlways(StrangerMessageEvent.class, event -> {
            final StrangerChatEvent e = new StrangerChatEvent(b, Objects.requireNonNull(SimpleStranger.get(b, event.getSender())), MessageChain.of(event.getMessage()), MessageSource.of(event.getSource()));
            try {
                EventManager.submit(e);
            } catch (final EventSubmitException ex) {
                FocessQQ.getLogger().thrLang("exception-submit-stranger-chat-event", ex);
            }
        }));
        listeners.add(bot.getEventChannel().subscribeAlways(MessagePostSendEvent.class, event -> {
            final BotSendMessageEvent e = new BotSendMessageEvent(b, Message.of(event.getMessage()), Objects.requireNonNull(SimpleContact.get(b, event.getTarget())));
            try {
                EventManager.submit(e);
            } catch (final EventSubmitException ex) {
                FocessQQ.getLogger().thrLang("exception-submit-bot-send-message-event", ex);
            }
        }));
        listeners.add(bot.getEventChannel().subscribeAlways(MessagePreSendEvent.class, event -> {
            final BotPreSendMessageEvent e = new BotPreSendMessageEvent(b, Message.of(event.getMessage()), Objects.requireNonNull(SimpleContact.get(b, event.getTarget())), event);
            try {
                EventManager.submit(e);
            } catch (final EventSubmitException ex) {
                FocessQQ.getLogger().thrLang("exception-submit-bot-pre-send-message-event", ex);
            }
        }));
        listeners.add(bot.getEventChannel().subscribeAlways(MessageSyncEvent.class, event -> {
            final BotSendMessageEvent e = new BotSendMessageEvent(b, Message.of(event.getMessage()), Objects.requireNonNull(SimpleContact.get(b, event.getSubject())));
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
        if (!bot.isOnline())
            return false;
        bot.getNativeBot().close();
        for (final Listener<?> listener : BOT_LISTENER_MAP.getOrDefault(bot, Lists.newArrayList()))
            listener.complete();
        BOT_LISTENER_MAP.remove(bot);
        SimpleFriend.remove(bot);
        SimpleGroup.remove(bot);
        SimpleStranger.remove(bot);
        SimpleMember.remove(bot);
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
        final boolean ret = this.logout(bot) & this.login(bot);
        try {
            EventManager.submit(new BotReloginEvent(bot));
        } catch (final EventSubmitException e) {
            FocessQQ.getLogger().thrLang("exception-submit-bot-relogin-event", e);
        }
        return ret;
    }

    @Override
    public List<Bot> getBots() {
        return Lists.newArrayList(BOTS.values());
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


}
