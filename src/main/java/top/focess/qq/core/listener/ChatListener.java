package top.focess.qq.core.listener;

import top.focess.qq.FocessQQ;
import top.focess.qq.api.event.EventHandler;
import top.focess.qq.api.command.CommandSender;
import top.focess.qq.api.event.EventManager;
import top.focess.qq.api.event.EventPriority;
import top.focess.qq.api.event.Listener;
import top.focess.qq.api.event.chat.FriendChatEvent;
import top.focess.qq.api.event.chat.GroupChatEvent;
import top.focess.qq.api.event.chat.StrangerChatEvent;
import top.focess.qq.api.event.message.FriendMessageEvent;
import top.focess.qq.api.event.message.GroupMessageEvent;
import top.focess.qq.api.event.message.StrangerMessageEvent;
import top.focess.qq.api.exceptions.EventSubmitException;
import top.focess.qq.api.exceptions.InputTimeoutException;
import top.focess.qq.api.schedule.Scheduler;
import top.focess.qq.api.schedule.Schedulers;
import top.focess.qq.api.util.IOHandler;
import top.focess.qq.api.util.Pair;
import com.google.common.collect.Maps;
import com.google.common.collect.Queues;

import java.util.Map;
import java.util.Queue;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

public class ChatListener implements Listener {
    private static final Scheduler EXECUTOR = Schedulers.newThreadPoolScheduler(FocessQQ.getMainPlugin(),10);

    public static final Map<CommandSender, Queue<Pair<IOHandler, Pair<Boolean,Long>>>> QUESTS = Maps.newHashMap();

    /**
     * Register input String listener. (Used to communicate with CommandSender with ioHandler)
     *
     * @param ioHandler the receiver
     * @param commandSender the commandSender
     * @param flag true if you want to get the string value of this message, false if you want to get MiraiCode of this message
     */
    public static void registerInputListener(IOHandler ioHandler, CommandSender commandSender, boolean flag) {
        synchronized (QUESTS) {
            QUESTS.compute(commandSender, (k, v) -> {
                if (v == null)
                    v = Queues.newConcurrentLinkedQueue();
                v.offer(Pair.of(ioHandler, Pair.of(flag, System.currentTimeMillis())));
                return v;
            });
        }
    }

    private static void updateInput(CommandSender sender, String content, String miraiContent, AtomicBoolean flag) {
        synchronized (QUESTS) {
            QUESTS.compute(sender, (k, v) -> {
                if (v != null && !v.isEmpty()) {
                    Pair<IOHandler, Pair<Boolean, Long>> element = v.poll();
                    while (element != null && System.currentTimeMillis() - element.getValue().getValue() > 1000 * 60 * 10) {
                        element.getKey().input(null);
                        element = v.poll();
                    }
                    if (element == null)
                        return v;
                    if (element.getValue().getKey())
                        element.getKey().input(content);
                    else element.getKey().input(miraiContent);
                    flag.set(true);
                }
                return v;
            });
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onStrangerChat(StrangerChatEvent event) {
        FocessQQ.getLogger().debug(String.format("%s(%d)", event.getStranger().getNick(), event.getStranger().getId()));
        FocessQQ.getLogger().debugLang("message-chain");
        event.getMessage().stream().map(Object::toString).forEach(FocessQQ.getLogger()::debug);
        StrangerMessageEvent strangerMessageEvent = new StrangerMessageEvent(event.getBot(),event.getMessage(),event.getStranger());
        try {
            EventManager.submit(strangerMessageEvent);
        } catch (EventSubmitException e) {
            FocessQQ.getLogger().thrLang("exception-submit-stranger-message-event",e);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onGroupChat(GroupChatEvent event) {
        FocessQQ.getLogger().debug(String.format("%s(%d,%s) in %s(%d): %s", event.getMember().getNameCard(), event.getMember().getId(), event.getMember().getPermission(), event.getGroup().getName(), event.getGroup().getId(), event.getMessage()));
        FocessQQ.getLogger().debugLang("message-chain");
        event.getMessage().stream().map(Object::toString).forEach(FocessQQ.getLogger()::debug);
        CommandSender sender = new CommandSender(event.getMember());
        AtomicBoolean flag = new AtomicBoolean(false);
        updateInput(sender, event.getMessage().contentToString(), event.getMessage().serializeToMiraiCode(), flag);
        if (!flag.get())
            try {
                Future<Boolean> ret = FocessQQ.CommandLine.exec(sender, event.getMessage().contentToString());
                EXECUTOR.run(()->{
                    try {
                        if (!ret.get(10, TimeUnit.MINUTES)) {
                            GroupMessageEvent groupMessageEvent = new GroupMessageEvent(event.getBot(),event.getMember(),event.getMessage(),event.getSource());
                            try {
                                EventManager.submit(groupMessageEvent);
                            } catch (Exception e) {
                                FocessQQ.getLogger().thrLang("exception-submit-group-message-event", e);
                            }
                        }
                    } catch (Exception e) {
                        if (!(e instanceof InputTimeoutException) && !(e instanceof TimeoutException))
                            FocessQQ.getLogger().thrLang("exception-exec-group-command",e);
                    }
                });
            } catch (Exception e) {
                FocessQQ.getLogger().thrLang("exception-exec-group-command",e);
            }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onFriendChat(FriendChatEvent event){
        FocessQQ.getLogger().debug(String.format("%s(%d)", event.getFriend().getNick(), event.getFriend().getId()));
        FocessQQ.getLogger().debugLang("message-chain");
        event.getMessage().stream().map(Object::toString).forEach(FocessQQ.getLogger()::debug);
        CommandSender sender = new CommandSender(event.getFriend());
        AtomicBoolean flag = new AtomicBoolean(false);
        updateInput(sender, event.getMessage().contentToString(), event.getMessage().serializeToMiraiCode(), flag);
        if (!flag.get())
            try {
                Future<Boolean> ret = FocessQQ.CommandLine.exec(sender, event.getMessage().contentToString());
                EXECUTOR.run(()->{
                    try {
                        if (!ret.get(10,TimeUnit.MINUTES)) {
                            FriendMessageEvent friendMessageEvent = new FriendMessageEvent(event.getBot(),event.getFriend(),event.getMessage());
                            try {
                                EventManager.submit(friendMessageEvent);
                            } catch (Exception e) {
                                FocessQQ.getLogger().thrLang("exception-submit-friend-message-event", e);
                            }
                        }
                    } catch (Exception e) {
                        if (!(e instanceof InputTimeoutException) && !(e instanceof TimeoutException))
                            FocessQQ.getLogger().thrLang("exception-exec-friend-command",e);
                    }
                });
            } catch (Exception e) {
                FocessQQ.getLogger().thrLang("exception-exec-friend-command",e);
            }
    }
}
