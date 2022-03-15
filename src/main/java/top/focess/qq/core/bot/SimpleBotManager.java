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
import top.focess.qq.api.bot.BotManager;
import top.focess.qq.api.event.EventManager;
import top.focess.qq.api.event.bot.BotLoginEvent;
import top.focess.qq.api.event.bot.BotLogoutEvent;
import top.focess.qq.api.event.bot.BotReloginEvent;
import top.focess.qq.api.event.bot.FriendInputStatusEvent;
import top.focess.qq.api.event.chat.FriendChatEvent;
import top.focess.qq.api.event.chat.GroupChatEvent;
import top.focess.qq.api.event.chat.StrangerChatEvent;
import top.focess.qq.api.event.recall.FriendRecallEvent;
import top.focess.qq.api.event.recall.GroupRecallEvent;
import top.focess.qq.api.event.request.FriendRequestEvent;
import top.focess.qq.api.event.request.GroupRequestEvent;
import top.focess.qq.api.exceptions.BotLoginException;
import top.focess.qq.api.exceptions.EventSubmitException;
import top.focess.qq.api.schedule.Scheduler;
import top.focess.qq.api.schedule.Schedulers;
import top.focess.qq.api.util.IOHandler;

import javax.imageio.stream.FileImageOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;

public class SimpleBotManager implements BotManager {

    private static final Scheduler SCHEDULER = Schedulers.newFocessScheduler(FocessQQ.getMainPlugin(), "BotManager");

    private static final Map<Bot,List<Listener<?>>> BOT_LISTENER_MAP = Maps.newHashMap();

    private static final Map<Long,Bot> BOTS = Maps.newConcurrentMap();

    public SimpleBotManager() {
        if (FocessQQ.getBotManager() != null) {
            FocessQQ.getLogger().fatalLang("fatal-simple-bot-manager-already-exist");
            FocessQQ.exit();
        }
    }

    @Override
    public @NotNull Future<Bot> login(long id, String password) {
        //todo add plugin argument
        return SCHEDULER.submit(() -> loginDirectly(id,password));
    }

    @Override
    public @NotNull Bot loginDirectly(long id, String password) {
        BotConfiguration configuration = BotConfiguration.getDefault();
        configuration.setProtocol(BotConfiguration.MiraiProtocol.ANDROID_PAD);
        File cache = new File("devices/" + id + "/cache");
        if (!cache.exists())
            if(!cache.mkdirs())
                throw new BotLoginException(id, FocessQQ.getLangConfig().get("fatal-create-cache-dir-failed"));
        configuration.fileBasedDeviceInfo("devices/" + id + "/device.json");
        configuration.setCacheDir(cache);
        configuration.setLoginSolver(new LoginSolver() {
            @Nullable
            @Override
            public Object onSolvePicCaptcha(@NotNull net.mamoe.mirai.Bot bot, @NotNull byte[] bytes, @NotNull Continuation<? super String> continuation) {
                try {
                    FileImageOutputStream outputStream = new FileImageOutputStream(new File("captcha.jpg"));
                    outputStream.write(bytes);
                    outputStream.close();
                } catch (IOException e) {
                    FocessQQ.getLogger().thrLang("exception-load-captcha-picture",e);
                }
                FocessQQ.getLogger().infoLang("input-captcha-code");
                return IOHandler.getConsoleIoHandler().input();
            }

            @Nullable
            @Override
            public Object onSolveSliderCaptcha(@NotNull net.mamoe.mirai.Bot bot, @NotNull String s, @NotNull Continuation<? super String> continuation) {
                FocessQQ.getLogger().info(s);
                IOHandler.getConsoleIoHandler().input();
                return null;
            }

            @Nullable
            @Override
            public Object onSolveUnsafeDeviceLoginVerify(@NotNull net.mamoe.mirai.Bot bot, @NotNull String s, @NotNull Continuation<? super String> continuation) {
                FocessQQ.getLogger().info(s);
                IOHandler.getConsoleIoHandler().input();
                return null;
            }
        });
        net.mamoe.mirai.Bot bot;
        try {
            bot = BotFactory.INSTANCE.newBot(id, password, configuration);
            bot.login();
        } catch(Exception e) {
            throw new BotLoginException(id,e);
        }
        Bot b = new SimpleBot(id,password, bot);
        try {
            EventManager.submit(new BotLoginEvent(b));
        } catch (EventSubmitException e) {
            FocessQQ.getLogger().thrLang("exception-submit-bot-login-event",e);
        }
        List<Listener<?>> listeners = Lists.newArrayList();
        listeners.add(bot.getEventChannel().subscribeAlways(GroupMessageEvent.class, event -> {
            GroupChatEvent e = new GroupChatEvent(b,event.getSender(), event.getMessage(),event.getSource());
            try {
                EventManager.submit(e);
            } catch (EventSubmitException eventSubmitException) {
                FocessQQ.getLogger().thrLang("exception-submit-group-chat-event",eventSubmitException);
            }
        }));
        listeners.add(bot.getEventChannel().subscribeAlways(FriendMessageEvent.class, event -> {
            FriendChatEvent e = new FriendChatEvent(b,event.getFriend(), event.getMessage(),event.getSource());
            try {
                EventManager.submit(e);
            } catch (EventSubmitException eventSubmitException) {
                FocessQQ.getLogger().thrLang("exception-submit-friend-chat-event",eventSubmitException);
            }
        }));
        listeners.add(bot.getEventChannel().subscribeAlways(MessageRecallEvent.GroupRecall.class, event -> {
            GroupRecallEvent e = new GroupRecallEvent(b,event.getAuthor(),event.getMessageIds(),event.getOperator());
            try {
                EventManager.submit(e);
            } catch (EventSubmitException ex) {
                FocessQQ.getLogger().thrLang("exception-submit-group-recall-event",ex);
            }
        }));
        listeners.add(bot.getEventChannel().subscribeAlways(MessageRecallEvent.FriendRecall.class, event -> {
            FriendRecallEvent e = new FriendRecallEvent(b,event.getAuthor(),event.getMessageIds());
            try {
                EventManager.submit(e);
            } catch (EventSubmitException ex) {
                FocessQQ.getLogger().thrLang("exception-submit-friend-recall-event",ex);
            }
        }));
        listeners.add(bot.getEventChannel().subscribeAlways(NewFriendRequestEvent.class, event ->{
            FriendRequestEvent e = new FriendRequestEvent(b,event.getFromId(),event.getFromNick(),event.getFromGroup(),event.getMessage());
            try {
                EventManager.submit(e);
            } catch (EventSubmitException ex) {
                FocessQQ.getLogger().thrLang("exception-submit-friend-request-event",ex);
            }
            if (e.getAccept() != null)
                if (e.getAccept())
                    event.accept();
                else event.reject(e.isBlackList());
        }));
        listeners.add(bot.getEventChannel().subscribeAlways(BotInvitedJoinGroupRequestEvent.class, event->{
            GroupRequestEvent e = new GroupRequestEvent(b,event.getGroupId(),event.getGroupName(),event.getInvitor());
            try {
                EventManager.submit(e);
            } catch (EventSubmitException ex) {
                FocessQQ.getLogger().thrLang("exception-submit-group-request-event",ex);
            }
            if (e.getAccept() != null)
                if (e.getAccept())
                    event.accept();
                else event.ignore();
        }));
        listeners.add(bot.getEventChannel().subscribeAlways(FriendInputStatusChangedEvent.class,event->{
            FriendInputStatusEvent e = new FriendInputStatusEvent(b,event.getFriend(), event.getInputting());
            try {
                EventManager.submit(e);
            } catch (EventSubmitException ex) {
                FocessQQ.getLogger().thrLang("exception-submit-friend-input-status-event",ex);
            }
        }));
        listeners.add(bot.getEventChannel().subscribeAlways(StrangerMessageEvent.class,event->{
            StrangerChatEvent e = new StrangerChatEvent(b,event.getMessage(),event.getSender(),event.getSource());
            try {
                EventManager.submit(e);
            } catch (EventSubmitException ex) {
                FocessQQ.getLogger().thrLang("exception-submit-stranger-chat-event",ex);
            }
        }));
        BOT_LISTENER_MAP.put(b,listeners);
        BOTS.put(id,b);
        return b;
    }

    @Override
    public boolean login(Bot b) {
        if (!b.isOnline() && b instanceof SimpleBot) {
            long id = b.getId();
            BotConfiguration configuration = BotConfiguration.getDefault();
            configuration.setProtocol(BotConfiguration.MiraiProtocol.ANDROID_PAD);
            File cache = new File("devices/" + id + "/cache");
            if (!cache.exists())
                if(!cache.mkdirs())
                    throw new BotLoginException(id, FocessQQ.getLangConfig().get("fatal-create-cache-dir-failed"));
            configuration.fileBasedDeviceInfo("devices/" + id + "/device.json");
            configuration.setCacheDir(cache);
            configuration.setLoginSolver(new LoginSolver() {
                @Nullable
                @Override
                public Object onSolvePicCaptcha(@NotNull net.mamoe.mirai.Bot bot, @NotNull byte[] bytes, @NotNull Continuation<? super String> continuation) {
                    try {
                        FileImageOutputStream outputStream = new FileImageOutputStream(new File("captcha.jpg"));
                        outputStream.write(bytes);
                        outputStream.close();
                    } catch (IOException e) {
                        FocessQQ.getLogger().thrLang("exception-load-captcha-picture",e);
                    }
                    FocessQQ.getLogger().infoLang("input-captcha-code");
                    return IOHandler.getConsoleIoHandler().input();
                }

                @Nullable
                @Override
                public Object onSolveSliderCaptcha(@NotNull net.mamoe.mirai.Bot bot, @NotNull String s, @NotNull Continuation<? super String> continuation) {
                    FocessQQ.getLogger().info(s);
                    IOHandler.getConsoleIoHandler().input();
                    return null;
                }

                @Nullable
                @Override
                public Object onSolveUnsafeDeviceLoginVerify(@NotNull net.mamoe.mirai.Bot bot, @NotNull String s, @NotNull Continuation<? super String> continuation) {
                    FocessQQ.getLogger().info(s);
                    IOHandler.getConsoleIoHandler().input();
                    return null;
                }
            });
            net.mamoe.mirai.Bot bot;
            try {
                bot = BotFactory.INSTANCE.newBot(id, ((SimpleBot) b).getPassword(), configuration);
                bot.login();
                ((SimpleBot) b).setNativeBot(bot);
            } catch(Exception e) {
                throw new BotLoginException(id,e);
            }
            try {
                EventManager.submit(new BotLoginEvent(b));
            } catch (EventSubmitException e) {
                FocessQQ.getLogger().thrLang("exception-submit-bot-login-event",e);
            }
            List<Listener<?>> listeners = Lists.newArrayList();
            listeners.add(bot.getEventChannel().subscribeAlways(GroupMessageEvent.class, event -> {
                GroupChatEvent e = new GroupChatEvent(b,event.getSender(), event.getMessage(),event.getSource());
                try {
                    EventManager.submit(e);
                } catch (EventSubmitException eventSubmitException) {
                    FocessQQ.getLogger().thrLang("exception-submit-group-chat-event",eventSubmitException);
                }
            }));
            listeners.add(bot.getEventChannel().subscribeAlways(FriendMessageEvent.class, event -> {
                FriendChatEvent e = new FriendChatEvent(b,event.getFriend(), event.getMessage(),event.getSource());
                try {
                    EventManager.submit(e);
                } catch (EventSubmitException eventSubmitException) {
                    FocessQQ.getLogger().thrLang("exception-submit-friend-chat-event",eventSubmitException);
                }
            }));
            listeners.add(bot.getEventChannel().subscribeAlways(MessageRecallEvent.GroupRecall.class, event -> {
                GroupRecallEvent e = new GroupRecallEvent(b,event.getAuthor(),event.getMessageIds(),event.getOperator());
                try {
                    EventManager.submit(e);
                } catch (EventSubmitException ex) {
                    FocessQQ.getLogger().thrLang("exception-submit-group-recall-event",ex);
                }
            }));
            listeners.add(bot.getEventChannel().subscribeAlways(MessageRecallEvent.FriendRecall.class, event -> {
                FriendRecallEvent e = new FriendRecallEvent(b,event.getAuthor(),event.getMessageIds());
                try {
                    EventManager.submit(e);
                } catch (EventSubmitException ex) {
                    FocessQQ.getLogger().thrLang("exception-submit-friend-recall-event",ex);
                }
            }));
            listeners.add(bot.getEventChannel().subscribeAlways(NewFriendRequestEvent.class, event ->{
                FriendRequestEvent e = new FriendRequestEvent(b,event.getFromId(),event.getFromNick(),event.getFromGroup(),event.getMessage());
                try {
                    EventManager.submit(e);
                } catch (EventSubmitException ex) {
                    FocessQQ.getLogger().thrLang("exception-submit-friend-request-event",ex);
                }
                if (e.getAccept() != null)
                    if (e.getAccept())
                        event.accept();
                    else event.reject(e.isBlackList());
            }));
            listeners.add(bot.getEventChannel().subscribeAlways(BotInvitedJoinGroupRequestEvent.class, event->{
                GroupRequestEvent e = new GroupRequestEvent(b,event.getGroupId(),event.getGroupName(),event.getInvitor());
                try {
                    EventManager.submit(e);
                } catch (EventSubmitException ex) {
                    FocessQQ.getLogger().thrLang("exception-submit-group-request-event",ex);
                }
                if (e.getAccept() != null)
                    if (e.getAccept())
                        event.accept();
                    else event.ignore();
            }));
            listeners.add(bot.getEventChannel().subscribeAlways(FriendInputStatusChangedEvent.class,event->{
                FriendInputStatusEvent e = new FriendInputStatusEvent(b,event.getFriend(), event.getInputting());
                try {
                    EventManager.submit(e);
                } catch (EventSubmitException ex) {
                    FocessQQ.getLogger().thrLang("exception-submit-friend-input-status-event",ex);
                }
            }));
            listeners.add(bot.getEventChannel().subscribeAlways(StrangerMessageEvent.class,event->{
                StrangerChatEvent e = new StrangerChatEvent(b,event.getMessage(),event.getSender(),event.getSource());
                try {
                    EventManager.submit(e);
                } catch (EventSubmitException ex) {
                    FocessQQ.getLogger().thrLang("exception-submit-stranger-chat-event",ex);
                }
            }));
            BOT_LISTENER_MAP.put(b,listeners);
            return true;
        }
        return false;
    }


    @Override
    public boolean logout(@NotNull Bot bot) {
        if (!bot.isOnline())
            return false;
        bot.getNativeBot().close();
        for (Listener<?> listener : BOT_LISTENER_MAP.getOrDefault(bot,Lists.newArrayList()))
            listener.complete();
        BOT_LISTENER_MAP.remove(bot);
        try {
            EventManager.submit(new BotLogoutEvent(bot));
        } catch (EventSubmitException e) {
            FocessQQ.getLogger().thrLang("exception-submit-bot-logout-event",e);
        }
        return true;
    }

    @Override
    public @Nullable Bot getBot(long username) {
        return BOTS.get(username);
    }

    @Override
    public boolean relogin(@NotNull Bot bot) {
        boolean ret = this.logout(bot) & this.login(bot);
        try {
            EventManager.submit(new BotReloginEvent(bot));
        } catch (EventSubmitException e) {
            FocessQQ.getLogger().thrLang("exception-submit-bot-relogin-event",e);
        }
        return ret;
    }

    @Override
    public List<Bot> getBots() {
        return Lists.newArrayList(BOTS.values());
    }

    @Override
    public Bot remove(long id) {
        if (FocessQQ.getBot().getId() == id)
            return null;
        Bot b = BOTS.remove(id);
        if (b != null)
            b.logout();
        return b;
    }

    public static void removeAll() {
        for (Long id : BOTS.keySet())
            FocessQQ.getBotManager().remove(id);
        //remove default bot
        Bot b = BOTS.remove(FocessQQ.getBot().getId());
        if (b != null)
            b.logout();
    }


}
