package top.focess.qq.core.listeners;

import com.google.common.collect.Maps;
import com.google.common.collect.Queues;
import top.focess.qq.FocessQQ;
import top.focess.qq.api.command.CommandLine;
import top.focess.qq.api.command.CommandResult;
import top.focess.qq.api.command.CommandSender;
import top.focess.qq.api.event.EventHandler;
import top.focess.qq.api.event.EventManager;
import top.focess.qq.api.event.EventPriority;
import top.focess.qq.api.event.Listener;
import top.focess.qq.api.event.chat.FriendChatEvent;
import top.focess.qq.api.event.chat.GroupChatEvent;
import top.focess.qq.api.event.chat.StrangerChatEvent;
import top.focess.qq.api.event.message.FriendMessageEvent;
import top.focess.qq.api.event.message.GroupMessageEvent;
import top.focess.qq.api.event.message.StrangerMessageEvent;
import top.focess.qq.api.event.EventSubmitException;
import top.focess.qq.api.util.InputTimeoutException;
import top.focess.qq.api.schedule.Scheduler;
import top.focess.qq.api.schedule.Schedulers;
import top.focess.qq.api.util.IOHandler;
import top.focess.qq.api.util.Pair;
import top.focess.qq.core.debug.Section;

import java.time.Duration;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;

public class ChatListener implements Listener {
    private static final Scheduler EXECUTOR = Schedulers.newThreadPoolScheduler(FocessQQ.getMainPlugin(),5,true,"ChatListener");

    public static final Map<CommandSender, Queue<Pair<IOHandler, Pair<Boolean,Long>>>> QUESTS = Maps.newConcurrentMap();

    /**
     * Register input String listener. (Used to communicate with CommandSender with ioHandler)
     *
     * @param ioHandler the receiver
     * @param commandSender the commandSender
     * @param flag true if you want to get the string value of this message, false if you want to get MiraiCode of this message
     */
    public static void registerInputListener(IOHandler ioHandler, CommandSender commandSender, boolean flag) {
        QUESTS.compute(commandSender, (k, v) -> {
            if (v == null)
                v = Queues.newLinkedBlockingDeque();
            v.offer(Pair.of(ioHandler, Pair.of(flag, System.currentTimeMillis())));
            return v;
        });
    }

    private static void updateInput(CommandSender sender, String content, String miraiContent, AtomicBoolean flag) {
        QUESTS.compute(sender, (k, v) -> {
            if (v != null) {
                Pair<IOHandler, Pair<Boolean, Long>> element = v.poll();
                while (element != null && System.currentTimeMillis() - element.getValue().getValue() > 1000 * 60 * 5) {
                    element.getKey().input(null);
                    element = v.poll();
                }
                if (element == null)
                    return v;
                if (element.getValue().getKey())
                    element.getKey().input(miraiContent);
                else element.getKey().input(content);
                flag.set(true);
            }
            return v;
        });
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onStrangerChat(StrangerChatEvent event) {
        IOHandler.getConsoleIoHandler().output(String.format("%s(%d)", event.getStranger().getNick(), event.getStranger().getId()));
        IOHandler.getConsoleIoHandler().outputLang("message-chain");
        event.getMessage().stream().map(Object::toString).forEach(IOHandler.getConsoleIoHandler()::output);
        StrangerMessageEvent strangerMessageEvent = new StrangerMessageEvent(event.getBot(),event.getMessage(),event.getStranger());
        try {
            EventManager.submit(strangerMessageEvent);
        } catch (EventSubmitException e) {
            FocessQQ.getLogger().thrLang("exception-submit-stranger-message-event",e);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onGroupChat(GroupChatEvent event) {
        IOHandler.getConsoleIoHandler().output(String.format("%s(%d,%s) in %s(%d): %s", event.getMember().getCardName(), event.getMember().getId(), event.getMember().getPermission(), event.getGroup().getName(), event.getGroup().getId(), event.getMessage()));
        IOHandler.getConsoleIoHandler().outputLang("message-chain");
        event.getMessage().stream().map(Object::toString).forEach(IOHandler.getConsoleIoHandler()::output);
        CommandSender sender = new CommandSender(event.getMember());
        AtomicBoolean flag = new AtomicBoolean(false);
        updateInput(sender, event.getMessage().contentToString(), event.getMessage().serializeToMiraiCode(), flag);
        if (!flag.get())
            try {
                Future<CommandResult> ret = CommandLine.exec(sender, event.getMessage().contentToString());
                EXECUTOR.run(()->{
                    Section section = Section.startSection("command-group-exec",ret, Duration.ofMinutes(10));
                    try {
                        if (ret.get() == CommandResult.NONE) {
                            GroupMessageEvent groupMessageEvent = new GroupMessageEvent(event.getBot(),event.getMember(),event.getMessage(),event.getSource());
                            try {
                                EventManager.submit(groupMessageEvent);
                            } catch (Exception e) {
                                FocessQQ.getLogger().thrLang("exception-submit-group-message-event", e);
                            }
                        }
                    } catch (Exception e) {
                        if (!(e.getCause() instanceof InputTimeoutException))
                            FocessQQ.getLogger().thrLang("exception-exec-group-command",e);
                    }
                    section.stop();
                });
            } catch (Exception e) {
                FocessQQ.getLogger().thrLang("exception-exec-group-command",e);
            }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onFriendChat(FriendChatEvent event){
        IOHandler.getConsoleIoHandler().output(String.format("%s(%d)", event.getFriend().getRawName(), event.getFriend().getId()));
        IOHandler.getConsoleIoHandler().outputLang("message-chain");
        event.getMessage().stream().map(Object::toString).forEach(IOHandler.getConsoleIoHandler()::output);
        CommandSender sender = new CommandSender(event.getFriend());
        AtomicBoolean flag = new AtomicBoolean(false);
        updateInput(sender, event.getMessage().contentToString(), event.getMessage().serializeToMiraiCode(), flag);
        if (!flag.get())
            try {
                Future<CommandResult> ret = CommandLine.exec(sender, event.getMessage().contentToString());
                EXECUTOR.run(()->{
                    Section section = Section.startSection("command-friend-exec",ret, Duration.ofMinutes(10));
                    try {
                        if (ret.get() == CommandResult.NONE) {
                            FriendMessageEvent friendMessageEvent = new FriendMessageEvent(event.getBot(),event.getFriend(),event.getMessage());
                            try {
                                EventManager.submit(friendMessageEvent);
                            } catch (Exception e) {
                                FocessQQ.getLogger().thrLang("exception-submit-friend-message-event", e);
                            }
                        }
                    } catch (Exception e) {
                        if (!(e.getCause() instanceof InputTimeoutException))
                            FocessQQ.getLogger().thrLang("exception-exec-friend-command",e);
                    }
                    section.stop();
                });
            } catch (Exception e) {
                FocessQQ.getLogger().thrLang("exception-exec-friend-command",e);
            }
    }
}