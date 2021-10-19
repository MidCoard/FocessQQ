package com.focess.listener;

import com.focess.Main;
import com.focess.api.annotation.EventHandler;
import com.focess.api.command.CommandSender;
import com.focess.api.event.EventManager;
import com.focess.api.event.EventPriority;
import com.focess.api.event.Listener;
import com.focess.api.event.chat.FriendChatEvent;
import com.focess.api.event.chat.GroupChatEvent;
import com.focess.api.event.message.ConsoleMessageEvent;
import com.focess.api.event.message.FriendMessageEvent;
import com.focess.api.event.message.GroupMessageEvent;
import com.focess.api.exceptions.InputTimeoutException;
import com.focess.api.util.IOHandler;
import com.focess.util.Pair;
import com.google.common.collect.Maps;
import com.google.common.collect.Queues;

import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;

public class ChatListener implements Listener {
    private static final ExecutorService EXECUTOR = Executors.newFixedThreadPool(10);

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
    public void onGroupChat(GroupChatEvent event) {
        Main.getLogger().debug(String.format("%s(%d,%s) in %s(%d): %s", event.getMember().getNameCard(), event.getMember().getId(), event.getMember().getPermission(), event.getGroup().getName(), event.getGroup().getId(), event.getMessage()));
        Main.getLogger().debug("MessageChain: ");
        event.getMessage().stream().map(Object::toString).forEach(Main.getLogger()::debug);
        CommandSender sender = new CommandSender(event.getMember());
        AtomicBoolean flag = new AtomicBoolean(false);
        updateInput(sender, event.getMessage().contentToString(), event.getMessage().serializeToMiraiCode(), flag);
        if (!flag.get())
            try {
                Future<Boolean> ret = Main.CommandLine.exec(sender, event.getMessage().contentToString());
                EXECUTOR.submit(()->{
                    try {
                        if (!ret.get()) {
                            GroupMessageEvent groupMessageEvent = new GroupMessageEvent(event.getMember(),event.getMessage(),event.getSource());
                            try {
                                EventManager.submit(groupMessageEvent);
                            } catch (Exception e) {
                                Main.getLogger().thr("Submit Group Message Exception", e);
                            }
                        }
                    } catch (Exception e) {
                        if (!(e instanceof InputTimeoutException))
                            Main.getLogger().thr("Group Exec Command Exception",e);
                    }
                });
            } catch (Exception e) {
                Main.getLogger().thr("Group Exec Command Exception",e);
            }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onFriendChat(FriendChatEvent event){
        Main.getLogger().debug(String.format("%s(%d)", event.getFriend().getNick(), event.getFriend().getId()));
        Main.getLogger().debug("MessageChain: ");
        event.getMessage().stream().map(Object::toString).forEach(Main.getLogger()::debug);
        CommandSender sender = new CommandSender(event.getFriend());
        AtomicBoolean flag = new AtomicBoolean(false);
        updateInput(sender, event.getMessage().contentToString(), event.getMessage().serializeToMiraiCode(), flag);
        if (!flag.get())
            try {
                Future<Boolean> ret = Main.CommandLine.exec(sender, event.getMessage().contentToString());
                EXECUTOR.submit(()->{
                    try {
                        if (!ret.get()) {
                            FriendMessageEvent friendMessageEvent = new FriendMessageEvent(event.getFriend(),event.getMessage());
                            try {
                                EventManager.submit(friendMessageEvent);
                            } catch (Exception e) {
                                Main.getLogger().thr("Submit Friend Message Exception", e);
                            }
                        }
                    } catch (Exception e) {
                        if (!(e instanceof InputTimeoutException))
                            Main.getLogger().thr("Friend Exec Command Exception",e);
                    }
                });
            } catch (Exception e) {
                Main.getLogger().thr("Friend Exec Command Exception",e);
            }
    }
}
