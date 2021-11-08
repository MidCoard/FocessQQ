package com.focess.core.listener;

import com.focess.Main;
import com.focess.api.annotation.EventHandler;
import com.focess.api.event.EventManager;
import com.focess.api.event.EventPriority;
import com.focess.api.event.Listener;
import com.focess.api.event.chat.ConsoleChatEvent;
import com.focess.api.event.message.ConsoleMessageEvent;
import com.focess.api.exceptions.InputTimeoutException;
import com.focess.api.util.IOHandler;
import com.focess.api.util.Pair;
import com.google.common.collect.Lists;

import java.util.Queue;
import java.util.concurrent.*;

public class ConsoleListener implements Listener {

    private static final ExecutorService EXECUTOR = Executors.newFixedThreadPool(10);
    public static final Queue<Pair<IOHandler,Long>> QUESTS = Lists.newLinkedList();

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onConsoleChat(ConsoleChatEvent event) {
        synchronized (ConsoleListener.QUESTS) {
            if (!QUESTS.isEmpty()) {
                Pair<IOHandler, Long> element = QUESTS.poll();
                while (element != null && System.currentTimeMillis() - element.getValue() > 60 * 10 * 1000) {
                    element.getKey().input(null);
                    element = QUESTS.poll();
                }
                if (element == null)
                    return;
                element.getKey().input(event.getMessage());
                return;
            }
        }
        try {
            Future<Boolean> ret = Main.CommandLine.exec(event.getMessage());
            EXECUTOR.submit(()->{
                try {
                    if (!ret.get(10, TimeUnit.MINUTES)) {
                        ConsoleMessageEvent consoleMessageEvent = new ConsoleMessageEvent(event.getMessage());
                        try {
                            EventManager.submit(consoleMessageEvent);
                        } catch (Exception e) {
                            Main.getLogger().thr("Submit Console Message Exception", e);
                        }
                    }
                } catch (Exception e) {
                    if (!(e instanceof InputTimeoutException) && !(e instanceof TimeoutException))
                        Main.getLogger().thr("Console Exec Command Exception",e);
                }
            });
        } catch (Exception e) {
            Main.getLogger().thr("Console Exec Command Exception", e);
        }
    }

    /**
     * Register input String listener. (Used to communicate with CommandSender with ioHandler)
     *
     * @param ioHandler the {@link com.focess.api.command.CommandSender#CONSOLE} CommandSender
     */
    public static void registerInputListener(IOHandler ioHandler) {
        QUESTS.add(Pair.of(ioHandler,System.currentTimeMillis()));
    }


}
