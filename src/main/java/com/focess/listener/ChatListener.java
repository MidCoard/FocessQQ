package com.focess.listener;

import com.focess.Main;
import com.focess.api.annotation.EventHandler;
import com.focess.api.command.CommandSender;
import com.focess.api.event.EventPriority;
import com.focess.api.event.Listener;
import com.focess.api.event.chat.FriendChatEvent;
import com.focess.api.event.chat.GroupChatEvent;
import com.focess.api.util.IOHandler;
import com.focess.util.Pair;
import com.google.common.collect.Maps;
import com.google.common.collect.Queues;

import java.util.Map;
import java.util.Queue;
import java.util.concurrent.atomic.AtomicBoolean;

public class ChatListener implements Listener {

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
            Main.CommandLine.exec(sender, event.getMessage().contentToString());
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
            Main.CommandLine.exec(sender, event.getMessage().contentToString());
    }
}
