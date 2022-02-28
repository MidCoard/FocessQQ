package top.focess.qq.core.listener;

import top.focess.qq.Main;
import top.focess.qq.api.event.EventHandler;
import top.focess.qq.api.event.EventManager;
import top.focess.qq.api.event.EventPriority;
import top.focess.qq.api.event.Listener;
import top.focess.qq.api.command.CommandSender;
import top.focess.qq.api.event.chat.ConsoleChatEvent;
import top.focess.qq.api.event.message.ConsoleMessageEvent;
import top.focess.qq.api.exceptions.InputTimeoutException;
import top.focess.qq.api.util.IOHandler;
import top.focess.qq.api.util.Pair;
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
                            Main.getLogger().thrLang("exception-submit-console-message-event", e);
                        }
                    }
                } catch (Exception e) {
                    if (!(e instanceof InputTimeoutException) && !(e instanceof TimeoutException))
                        Main.getLogger().thrLang("exception-exec-console-command",e);
                }
            });
        } catch (Exception e) {
            Main.getLogger().thrLang("exception-exec-console-command",e);
        }
    }

    /**
     * Register input String listener. (Used to communicate with CommandSender with ioHandler)
     *
     * @param ioHandler the {@link CommandSender#CONSOLE} CommandSender
     */
    public static void registerInputListener(IOHandler ioHandler) {
        QUESTS.add(Pair.of(ioHandler,System.currentTimeMillis()));
    }


}
