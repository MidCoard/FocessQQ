package top.focess.qq.api.command;

import com.google.common.collect.Lists;
import org.jetbrains.annotations.NotNull;
import top.focess.qq.FocessQQ;
import top.focess.qq.api.event.EventManager;
import top.focess.qq.api.event.command.CommandPrepostEvent;
import top.focess.qq.api.exceptions.EventSubmitException;
import top.focess.qq.api.schedule.Scheduler;
import top.focess.qq.api.schedule.Schedulers;
import top.focess.qq.api.util.CombinedFuture;
import top.focess.qq.api.util.IOHandler;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;

/**
 * The CommandLine Tool Class can be used to exec command with customize executor, arguments and receiver.
 */
public class CommandLine {


    private static final Scheduler EXECUTOR = Schedulers.newThreadPoolScheduler(FocessQQ.getMainPlugin(),10,false,"CommandLine");

    /**
     * Execute command using {@link CommandSender#CONSOLE}
     *
     * @param command the command Console executes.
     * @return a Future representing pending completion of the command
     */
    @NotNull
    public static Future<Boolean> exec(String command) {
        return exec(CommandSender.CONSOLE, command);
    }

    /**
     * Execute command with sender
     *
     * @param sender  the executor
     * @param command the command CommandSender executes.
     * @return a Future representing pending completion of the command
     */
    @NotNull
    public static Future<Boolean> exec(CommandSender sender, String command) {
        return exec(sender, command, sender.getIOHandler());
    }

    /**
     * Execute command with sender executing and ioHandler receiving
     *
     * @param sender    the executor
     * @param command   the command CommandSender executes.
     * @param ioHandler the receiver
     * @return a Future representing pending completion of the command
     */
    @NotNull
    public static Future<Boolean> exec(CommandSender sender, String command, IOHandler ioHandler) {
        if (sender == CommandSender.CONSOLE)
            FocessQQ.getLogger().consoleInput(command);
        List<String> args = Lists.newArrayList();
        StringBuilder stringBuilder = new StringBuilder();
        boolean stack = false;
        boolean ignore = false;
        for (char c : command.toCharArray()) {
            if (ignore) {
                ignore = false;
                switch (c) {
                    case 'a':
                        stringBuilder.append((char) 7);
                        break;
                    case 'b':
                        stringBuilder.append((char) 8);
                        break;
                    case 'f':
                        stringBuilder.append((char) 12);
                        break;
                    case 'n':
                        stringBuilder.append((char) 10);
                        break;
                    case 'r':
                        stringBuilder.append((char) 13);
                        break;
                    case 't':
                        stringBuilder.append((char) 9);
                        break;
                    case 'v':
                        stringBuilder.append((char) 11);
                        break;
                    case '0':
                        stringBuilder.append((char) 0);
                        break;
                    default:
                        stringBuilder.append(c);
                        break;
                }
            } else if (c == '\\')
                ignore = true;
            else if (c == ' ') {
                if (!stack) {
                    if (stringBuilder.length() > 0) {
                        args.add(stringBuilder.toString());
                        stringBuilder.delete(0, stringBuilder.length());
                    }
                } else
                    stringBuilder.append(c);
            } else if (c == '"')
                stack = !stack;
            else
                stringBuilder.append(c);
        }
        if (stringBuilder.length() != 0)
            args.add(stringBuilder.toString());
        if (args.size() == 0)
            return CompletableFuture.completedFuture(false);
        String name = args.get(0);
        args.remove(0);
        return exec0(sender, name, args.toArray(new String[0]), ioHandler, command);
    }

    private static Future<Boolean> exec0(CommandSender sender, String command, String[] args, IOHandler ioHandler, String rawCommand) {
        boolean flag = false;
        CombinedFuture ret = new CombinedFuture();
        for (Command com : Command.getCommands())
            if (com.getAliases().stream().anyMatch(i -> i.equalsIgnoreCase(command)) || com.getName().equalsIgnoreCase(command)) {
                CommandPrepostEvent event = new CommandPrepostEvent(sender, com, args, ioHandler);
                try {
                    EventManager.submit(event);
                } catch (EventSubmitException e) {
                    FocessQQ.getLogger().thrLang("exception-submit-command-prepost-event", e);
                }
                if (event.isCancelled())
                    continue;
                if (sender != CommandSender.CONSOLE)
                    IOHandler.getConsoleIoHandler().outputLang("command-exec", sender.toString(), rawCommand);
                flag = true;
                ret.combine(EXECUTOR.submit(() -> com.execute(sender, args, ioHandler)));
            }
        if (!flag && sender == CommandSender.CONSOLE)
            ioHandler.outputLang("unknown-command", command);
        return ret;
    }
}
