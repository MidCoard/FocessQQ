package top.focess.qq.core.listener;

import com.google.common.collect.Queues;
import top.focess.qq.FocessQQ;
import top.focess.qq.api.command.CommandLine;
import top.focess.qq.api.command.CommandSender;
import top.focess.qq.api.event.EventHandler;
import top.focess.qq.api.event.EventManager;
import top.focess.qq.api.event.EventPriority;
import top.focess.qq.api.event.Listener;
import top.focess.qq.api.event.chat.ConsoleChatEvent;
import top.focess.qq.api.event.message.ConsoleMessageEvent;
import top.focess.qq.api.exceptions.InputTimeoutException;
import top.focess.qq.api.schedule.Scheduler;
import top.focess.qq.api.schedule.Schedulers;
import top.focess.qq.api.util.IOHandler;
import top.focess.qq.api.util.Pair;
import top.focess.qq.core.debug.Section;

import java.time.Duration;
import java.util.Queue;
import java.util.concurrent.Future;

public class ConsoleListener implements Listener {

    private static final Scheduler EXECUTOR = Schedulers.newThreadPoolScheduler(FocessQQ.getMainPlugin(),10);
    public static final Queue<Pair<IOHandler,Long>> QUESTS = Queues.newLinkedBlockingDeque();

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onConsoleChat(ConsoleChatEvent event) {
        if (!QUESTS.isEmpty()) {
            Pair<IOHandler, Long> element = QUESTS.poll();
            while (element != null && System.currentTimeMillis() - element.getValue() > 60 * 5 * 1000) {
                element.getKey().input(null);
                element = QUESTS.poll();
            }
            if (element == null)
                return;
            element.getKey().input(event.getMessage());
            return;
        }
        try {
            Future<Boolean> ret = CommandLine.exec(event.getMessage());
            EXECUTOR.run(()->{
                Section section = Section.startSection("command-console-exec",ret, Duration.ofMinutes(10));
                try {
                    if (!ret.get()) {
                        ConsoleMessageEvent consoleMessageEvent = new ConsoleMessageEvent(event.getMessage());
                        try {
                            EventManager.submit(consoleMessageEvent);
                        } catch (Exception e) {
                            FocessQQ.getLogger().thrLang("exception-submit-console-message-event", e);
                        }
                    }
                } catch (Exception e) {
                    if (!(e.getCause() instanceof InputTimeoutException))
                        FocessQQ.getLogger().thrLang("exception-exec-console-command",e);
                }
                section.stop();
            });
        } catch (Exception e) {
            FocessQQ.getLogger().thrLang("exception-exec-console-command",e);
        }
    }

    /**
     * Register input String listener. (Used to communicate with CommandSender with ioHandler)
     *
     * @param ioHandler the {@link CommandSender#CONSOLE} CommandSender
     */
    public static void registerInputListener(IOHandler ioHandler) {
        QUESTS.offer(Pair.of(ioHandler,System.currentTimeMillis()));
    }


}
