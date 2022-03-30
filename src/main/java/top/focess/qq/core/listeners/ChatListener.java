package top.focess.qq.core.listeners;

import com.google.common.collect.Maps;
import com.google.common.collect.Queues;
import top.focess.qq.FocessQQ;
import top.focess.qq.api.bot.contact.Friend;
import top.focess.qq.api.bot.contact.Group;
import top.focess.qq.api.command.CommandLine;
import top.focess.qq.api.command.CommandResult;
import top.focess.qq.api.command.CommandSender;
import top.focess.qq.api.event.*;
import top.focess.qq.api.event.bot.BotSendMessageEvent;
import top.focess.qq.api.event.chat.FriendChatEvent;
import top.focess.qq.api.event.chat.GroupChatEvent;
import top.focess.qq.api.event.chat.StrangerChatEvent;
import top.focess.qq.api.event.message.FriendMessageEvent;
import top.focess.qq.api.event.message.GroupMessageEvent;
import top.focess.qq.api.event.message.StrangerMessageEvent;
import top.focess.qq.api.schedule.Scheduler;
import top.focess.qq.api.schedule.Schedulers;
import top.focess.qq.api.util.IOHandler;
import top.focess.qq.api.util.InputTimeoutException;
import top.focess.qq.api.util.Pair;
import top.focess.qq.core.debug.Section;

import java.time.Duration;
import java.util.List;
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
    public static void registerInputListener(final IOHandler ioHandler, final CommandSender commandSender, final boolean flag) {
        QUESTS.compute(commandSender, (k, v) -> {
            if (v == null)
                v = Queues.newLinkedBlockingDeque();
            v.offer(Pair.of(ioHandler, Pair.of(flag, System.currentTimeMillis())));
            return v;
        });
    }

    private static void updateInput(final CommandSender sender, final String content, final String miraiContent, final AtomicBoolean flag) {
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
    public void onStrangerChat(final StrangerChatEvent event) {
        IOHandler.getConsoleIoHandler().output(String.format("%s(%d)", event.getStranger().getRawName(), event.getStranger().getId()));
        IOHandler.getConsoleIoHandler().outputLang("message-chain");
        event.getMessage().stream().map(Object::toString).filter(i->!i.isEmpty()).forEach(IOHandler.getConsoleIoHandler()::output);
        final StrangerMessageEvent strangerMessageEvent = new StrangerMessageEvent(event.getBot(),event.getMessage(),event.getStranger(),event.getSource());
        try {
            EventManager.submit(strangerMessageEvent);
        } catch (final EventSubmitException e) {
            FocessQQ.getLogger().thrLang("exception-submit-stranger-message-event",e);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onGroupChat(final GroupChatEvent event) {
        IOHandler.getConsoleIoHandler().output(String.format("%s(%d,%s) in %s(%d): %s", event.getMember().getCardName(), event.getMember().getId(), event.getMember().getPermission(), event.getGroup().getName(), event.getGroup().getId(), event.getMessage()));
        IOHandler.getConsoleIoHandler().outputLang("message-chain");
        event.getMessage().stream().map(Object::toString).filter(i->!i.isEmpty()).forEach(IOHandler.getConsoleIoHandler()::output);
        final CommandSender sender = new CommandSender(event.getMember());
        final AtomicBoolean flag = new AtomicBoolean(false);
        updateInput(sender, event.getMessage().toString(), event.getMessage().toMiraiCode(), flag);
        if (!flag.get())
            try {
                final Future<CommandResult> ret = CommandLine.exec(sender, event.getMessage().toString());
                EXECUTOR.run(()->{
                    final Section section = Section.startSection("command-group-exec",ret, Duration.ofMinutes(10));
                    try {
                        if (ret.get() == CommandResult.NONE) {
                            final GroupMessageEvent groupMessageEvent = new GroupMessageEvent(event.getBot(),event.getMember(),event.getMessage(),event.getSource());
                            try {
                                EventManager.submit(groupMessageEvent);
                            } catch (final Exception e) {
                                FocessQQ.getLogger().thrLang("exception-submit-group-message-event", e);
                            }
                        }
                    } catch (final Exception e) {
                        if (!(e.getCause() instanceof InputTimeoutException))
                            FocessQQ.getLogger().thrLang("exception-exec-group-command",e);
                    }
                    section.stop();
                });
            } catch (final Exception e) {
                FocessQQ.getLogger().thrLang("exception-exec-group-command",e);
            }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onFriendChat(final FriendChatEvent event){
        IOHandler.getConsoleIoHandler().output(String.format("%s(%d)", event.getFriend().getRawName(), event.getFriend().getId()));
        IOHandler.getConsoleIoHandler().outputLang("message-chain");
        event.getMessage().stream().map(Object::toString).filter(i->!i.isEmpty()).forEach(IOHandler.getConsoleIoHandler()::output);
        final CommandSender sender = new CommandSender(event.getFriend());
        final AtomicBoolean flag = new AtomicBoolean(false);
        updateInput(sender, event.getMessage().toString(), event.getMessage().toMiraiCode(), flag);
        if (!flag.get())
            try {
                final Future<CommandResult> ret = CommandLine.exec(sender, event.getMessage().toString());
                EXECUTOR.run(()->{
                    final Section section = Section.startSection("command-friend-exec",ret, Duration.ofMinutes(10));
                    try {
                        if (ret.get() == CommandResult.NONE) {
                            final FriendMessageEvent friendMessageEvent = new FriendMessageEvent(event.getBot(),event.getFriend(),event.getMessage(),event.getSource());
                            try {
                                EventManager.submit(friendMessageEvent);
                            } catch (final Exception e) {
                                FocessQQ.getLogger().thrLang("exception-submit-friend-message-event", e);
                            }
                        }
                    } catch (final Exception e) {
                        if (!(e.getCause() instanceof InputTimeoutException))
                            FocessQQ.getLogger().thrLang("exception-exec-friend-command",e);
                    }
                    section.stop();
                });
            } catch (final Exception e) {
                FocessQQ.getLogger().thrLang("exception-exec-friend-command",e);
            }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onBotSendMessage(final BotSendMessageEvent event) {
        final List<String> args = CommandLine.splitCommand(event.getMessage().toString());
        if (args.size() != 0 && args.get(0).equalsIgnoreCase("exec"))
            if (event.getContact() instanceof Friend)
                CommandLine.exec(new CommandSender((Friend) event.getContact()), event.getMessage().toString());
            else if (event.getContact() instanceof Group)
                CommandLine.exec(new CommandSender(((Group) event.getContact()).getAsMember()), event.getMessage().toString());
    }
}
