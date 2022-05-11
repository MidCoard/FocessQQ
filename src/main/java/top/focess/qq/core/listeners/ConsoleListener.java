package top.focess.qq.core.listeners;

import com.google.common.collect.Queues;
import top.focess.command.CommandResult;
import top.focess.command.InputTimeoutException;
import top.focess.qq.FocessQQ;
import top.focess.qq.api.command.CommandLine;
import top.focess.qq.api.command.CommandSender;
import top.focess.qq.api.event.EventHandler;
import top.focess.qq.api.event.EventManager;
import top.focess.qq.api.event.EventPriority;
import top.focess.qq.api.event.Listener;
import top.focess.qq.api.event.chat.ConsoleChatEvent;
import top.focess.qq.api.event.message.ConsoleMessageEvent;
import top.focess.qq.api.schedule.Schedulers;
import top.focess.qq.api.util.IOHandler;
import top.focess.qq.core.debug.Section;
import top.focess.scheduler.Scheduler;
import top.focess.scheduler.Task;
import top.focess.util.Pair;

import java.time.Duration;
import java.util.Queue;
import java.util.concurrent.Future;

public class ConsoleListener implements Listener {

    public static final Queue<Pair<IOHandler, Task>> QUESTS = Queues.newLinkedBlockingQueue();
    private static final Scheduler EXECUTOR = Schedulers.newThreadPoolScheduler(FocessQQ.getMainPlugin(), 5, true, "ConsoleListener");

    /**
     * Register input String listener. (Used to communicate with CommandSender with ioHandler)
     *
     * @param ioHandler the {@link CommandSender#CONSOLE} CommandSender
     * @param task the timeout task
     */
    public static void registerInputListener(final IOHandler ioHandler, final Task task) {
        QUESTS.offer(Pair.of(ioHandler,task));
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onConsoleChat(final ConsoleChatEvent event) {
        Pair<IOHandler,Task> element;
        while ((element = QUESTS.poll()) != null)
            if (element.getValue().cancel()) {
                element.getKey().input(event.getMessage());
                return;
            }
        try {
            final Future<CommandResult> ret = CommandLine.exec(event.getMessage().toString());
            EXECUTOR.run(() -> {
                final Section section = Section.startSection("command-console-exec", ret, Duration.ofMinutes(10));
                try {
                    if (ret.get() == CommandResult.NONE) {
                        final ConsoleMessageEvent consoleMessageEvent = new ConsoleMessageEvent(event.getMessage());
                        try {
                            EventManager.submit(consoleMessageEvent);
                        } catch (final Exception e) {
                            FocessQQ.getLogger().thrLang("exception-submit-console-message-event", e);
                        }
                    }
                } catch (final Exception e) {
                    if (!(e.getCause() instanceof InputTimeoutException))
                        FocessQQ.getLogger().thrLang("exception-exec-console-command", e);
                }
                section.stop();
            },"command-console-exec");
        } catch (final Exception e) {
            FocessQQ.getLogger().thrLang("exception-exec-console-command", e);
        }
    }


}
