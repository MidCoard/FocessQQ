package top.focess.qq.api.command;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import top.focess.command.CommandResult;
import top.focess.qq.FocessQQ;
import top.focess.qq.api.event.EventManager;
import top.focess.qq.api.event.EventSubmitException;
import top.focess.qq.api.event.command.CommandPrepostEvent;
import top.focess.qq.api.plugin.Plugin;
import top.focess.qq.api.schedule.Scheduler;
import top.focess.qq.api.schedule.Schedulers;
import top.focess.qq.api.util.IOHandler;
import top.focess.util.Pair;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * The CommandLine Tool Class can be used to exec command with customize executor, arguments and receiver.
 */
public class CommandLine {

    private static final AtomicInteger COMMAND_ID = new AtomicInteger();

    private static final Map<String, SpecialArgumentComplexHandler> SPECIAL_ARGUMENT_HANDLERS = Maps.newConcurrentMap();
    private static final Map<Plugin, List<Pair<String, SpecialArgumentComplexHandler>>> PLUGIN_SPECIAL_ARGUMENT_MAP = Maps.newConcurrentMap();

    private static final Scheduler EXECUTOR = Schedulers.newThreadPoolScheduler(FocessQQ.getMainPlugin(), 7, false, "CommandLine");

    /**
     * Execute command using {@link CommandSender#CONSOLE}
     *
     * @param command the command Console executes.
     * @return a Future representing pending completion of the command
     */
    @NotNull
    public static Future<CommandResult> exec(final String command) {
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
    public static Future<CommandResult> exec(final CommandSender sender, final String command) {
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
    public static Future<CommandResult> exec(final CommandSender sender, final String command, final IOHandler ioHandler) {
        // not check sender's bot
        int id = COMMAND_ID.getAndIncrement();
        FocessQQ.getLogger().debugLang("command-line-exec",sender.toString(), command, id);
        if (sender == CommandSender.CONSOLE)
            FocessQQ.getLogger().consoleInput(command);
        final List<String> args = splitCommand(command);
        if (args.size() == 0)
            return CompletableFuture.completedFuture(CommandResult.NONE);
        final String name = args.get(0);
        args.remove(0);
        return exec0(sender, name, args.toArray(new String[0]), ioHandler, command,id);
    }

    /**
     * Split the command into arguments
     *
     * @param command the command needed to be split
     * @return the split arguments
     */
    @NotNull
    public static List<String> splitCommand(@NotNull final String command) {
        final List<String> args = Lists.newArrayList();
        final StringBuilder stringBuilder = new StringBuilder();
        boolean stack = false;
        boolean ignore = false;
        Character last = null;
        for (final char c : command.toCharArray()) {
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
                    stringBuilder.append(' ');
            } else if (c == '"')
                stack = !stack;
            else if (c == '@' && !stack && last != null && last == ' ') {
                stringBuilder.append('"');
                stringBuilder.append('@');
            } else stringBuilder.append(c);
            last = c;
        }
        if (stringBuilder.length() != 0)
            args.add(stringBuilder.toString());
        return args;
    }

    @NotNull
    @Contract("_ -> new")
    private static Pair<String, String[]> splitSpecialArgument(@NotNull final String argument) {
        final int leftIndex = argument.indexOf('(');
        if (leftIndex == -1 || !argument.endsWith(")"))
            return new Pair<>(argument, new String[0]);
        final String name = argument.substring(0, leftIndex);
        final String[] args = argument.substring(leftIndex + 1, argument.length() - 1).split(",");
        return new Pair<>(name, args);
    }

    private static Future<CommandResult> exec0(final CommandSender sender, final String command, final String[] args, final IOHandler ioHandler, final String rawCommand, int id) {
        FocessQQ.getLogger().debugLang("command-pre-exec", sender.toString(), command, Arrays.toString(args),id);
        boolean flag = false;
        Future<CommandResult> ret = CompletableFuture.completedFuture(CommandResult.NONE);
        for (final Command com : Command.getCommands())
            if (com.getAliases().stream().anyMatch(i -> i.equalsIgnoreCase(command)) || com.getName().equalsIgnoreCase(command)) {
                FocessQQ.getLogger().debugLang("command-before-special-handler", sender.toString(), command, Arrays.toString(args),id);
                for (int i = 0; i < args.length; i++)
                    if (args[i].startsWith("\"@")) {
                        final String h = args[i].substring(2);
                        final Pair<String, String[]> pair = splitSpecialArgument(h);
                        final String head = pair.getKey();
                        final String[] values = pair.getValue();
                        if (SPECIAL_ARGUMENT_HANDLERS.containsKey(head))
                            args[i] = SPECIAL_ARGUMENT_HANDLERS.get(head).handle(head, sender, com, args, i, values);
                        else if (SPECIAL_ARGUMENT_HANDLERS.containsKey(com.getPlugin().getName() + ":" + head))
                            args[i] = SPECIAL_ARGUMENT_HANDLERS.get(com.getPlugin().getName() + ":" + head).handle(head, sender, com, args, i, values);
                        else args[i] = args[i].substring(1);
                    }
                FocessQQ.getLogger().debugLang("command-after-special-handler", sender.toString(), command, Arrays.toString(args),id);
                final CommandPrepostEvent event = new CommandPrepostEvent(sender, com, args, ioHandler);
                try {
                    EventManager.submit(event);
                } catch (final EventSubmitException e) {
                    FocessQQ.getLogger().thrLang("exception-submit-command-prepost-event", e);
                }
                // if not want to execute, it should be cancelled
                if (event.isCancelled())
                    continue;
                FocessQQ.getLogger().debugLang("command-before-exec", sender.toString(), command, Arrays.toString(args),id);
                sender.getSession().set("@previous_command", rawCommand);
                flag = true;
                ret = EXECUTOR.submit(() -> {
                    try {
                        CommandResult result = com.execute(sender, args, ioHandler);
                        FocessQQ.getLogger().debugLang("command-after-exec", sender.toString(), command, Arrays.toString(args), result.toString(), id);
                        return result;
                    } catch (final Exception e) {
                        ioHandler.outputLang("command-execute-exception", e.getMessage());
                        FocessQQ.getLogger().thrLang("exception-command-execute", e);
                        return CommandResult.REFUSE_EXCEPTION;
                    }
                });
                break;
            }
        if (!flag && sender == CommandSender.CONSOLE)
            ioHandler.outputLang("unknown-command", command);
        return ret;
    }

    /**
     * Register the special argument handler
     *
     * @param plugin  the plugin
     * @param name    the name of the special argument handler
     * @param handler the special argument handler
     */
    public static void register(final Plugin plugin, final String name, final SpecialArgumentComplexHandler handler) {
        PLUGIN_SPECIAL_ARGUMENT_MAP.compute(plugin, (k, v) -> {
            if (v == null)
                v = Lists.newArrayList();
            final String n = plugin == FocessQQ.getMainPlugin() ? name : plugin.getName() + ":" + name;
            v.removeIf(i -> i.getKey().equals(n));
            v.add(Pair.of(n, handler));
            SPECIAL_ARGUMENT_HANDLERS.put(n, handler);
            return v;
        });
    }

    /**
     * Unregister the special argument handler
     *
     * @param handler the special argument handler
     */
    public static void unregister(final SpecialArgumentComplexHandler handler) {
        PLUGIN_SPECIAL_ARGUMENT_MAP.forEach((k, v) -> v.removeIf(i -> i.getRight() == handler));
        SPECIAL_ARGUMENT_HANDLERS.forEach((k, v) -> {
            if (v == handler)
                SPECIAL_ARGUMENT_HANDLERS.remove(k);
        });
    }

    /**
     * Unregister the special argument handler
     *
     * @param plugin the plugin
     * @param name   the name of the special argument handler
     */
    public static void unregister(final Plugin plugin, final String name) {
        PLUGIN_SPECIAL_ARGUMENT_MAP.computeIfPresent(plugin, (k, v) -> {
            v.removeIf(i -> i.getLeft().equals(name));
            return v;
        });
        SPECIAL_ARGUMENT_HANDLERS.remove(name);
    }

    /**
     * Unregister the special argument handlers by plugin
     *
     * @param plugin the plugin
     */
    public static void unregister(final Plugin plugin) {
        for (final Pair<String, SpecialArgumentComplexHandler> pair : PLUGIN_SPECIAL_ARGUMENT_MAP.getOrDefault(plugin, Lists.newArrayList()))
            SPECIAL_ARGUMENT_HANDLERS.remove(pair.getLeft());
        PLUGIN_SPECIAL_ARGUMENT_MAP.remove(plugin);
    }

    /**
     * Unregister all the special argument handlers
     *
     * @return true if there are some special argument handlers not belonging to MainPlugin not been unregistered, false otherwise
     */
    public static boolean unregisterAll() {
        boolean flag = false;
        for (final Plugin plugin : PLUGIN_SPECIAL_ARGUMENT_MAP.keySet()) {
            if (plugin != FocessQQ.getMainPlugin())
                flag = true;
            unregister(plugin);
        }
        return flag;
    }
}
