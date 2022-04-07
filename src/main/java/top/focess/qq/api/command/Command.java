package top.focess.qq.api.command;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.jetbrains.annotations.NotNull;
import top.focess.command.CommandArgument;
import top.focess.command.CommandPermission;
import top.focess.command.CommandResult;
import top.focess.qq.FocessQQ;
import top.focess.qq.api.event.EventManager;
import top.focess.qq.api.event.EventSubmitException;
import top.focess.qq.api.event.command.CommandExecutedEvent;
import top.focess.qq.api.plugin.Plugin;
import top.focess.qq.api.util.IOHandler;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

/**
 * Represent a Plugin class that can execute. Just like we use the terminal, we could use it to executing some commands. This is an important way to interact with FocessQQ Bot.
 * You should declare {@link CommandType} to this class ,or you should register it with your plugin manually.
 */
public abstract class Command {


    private static final Map<String, Command> COMMANDS_MAP = Maps.newConcurrentMap();

    private top.focess.command.Command command;

    /**
     * The plugin the command belongs to
     */
    private Plugin plugin;

    /**
     * Instance a <code>Command</code> Class with special name and aliases.
     *
     * @param name    the name of the command
     * @param aliases the aliases of the command
     * @throws CommandLoadException if there is any exception thrown in the initializing process
     */
    public Command(@NotNull final String name, @NotNull final String... aliases) {
        this.command = new top.focess.command.Command(name, aliases){

            @Override
            public void init() {
            }

            @Override
            public @NotNull List<String> usage(top.focess.command.CommandSender commandSender) {
                return Command.this.usage((CommandSender) commandSender);
            }
        };
        Command.this.init();
    }

    /**
     * Provide a constructor to help {@link CommandType} design.
     * Never instance it!Command will be instanced when this class is loaded automatically.
     */
    protected Command() {
    }

    /**
     * Unregister all commands in the plugin
     *
     * @param plugin the plugin that the commands that need to be unregistered belongs to
     */
    public static void unregister(final Plugin plugin) {
        for (final Command command : COMMANDS_MAP.values())
            if (command.getPlugin().equals(plugin))
                command.unregister();
    }

    /**
     * Unregister all commands
     *
     * @return true if there are some commands not belonging to MainPlugin not been unregistered, false otherwise
     */
    public static boolean unregisterAll() {
        boolean ret = false;
        for (final Command command : COMMANDS_MAP.values()) {
            if (command.getPlugin() != FocessQQ.getMainPlugin())
                ret = true;
            command.unregister();
        }
        return ret;
    }

    /**
     * Get all commands
     *
     * @return All commands as a list
     */
    @NotNull
    public static List<Command> getCommands() {
        return Collections.unmodifiableList(Lists.newArrayList(COMMANDS_MAP.values()));
    }

    /**
     * Register the command
     *
     * @param plugin  the plugin the command belongs to
     * @param command the command that need to be registered
     * @throws CommandDuplicateException if the command name already exists in the registered commands
     * @throws IllegalStateException    if the command is not initialized
     */
    public static void register(@NotNull final Plugin plugin, @NotNull final Command command) {
        top.focess.command.Command.register(command.command);
        command.plugin = plugin;
        COMMANDS_MAP.put(command.getName(), command);
    }

    public boolean isRegistered() {
        return this.command.isRegistered();
    }

    @NotNull
    public Plugin getPlugin() {
        return this.plugin;
    }

    /**
     * Unregister this command
     */
    public void unregister() {
        this.command.unregister();
        COMMANDS_MAP.remove(this.getName());
    }

    @NotNull
    public String getName() {
        return this.command.getName();
    }

    @NotNull
    public List<String> getAliases() {
        return this.command.getAliases();
    }

    public Predicate<CommandSender> getExecutorPermission() {
        return i -> this.command.getExecutorPermission().test(i);
    }

    public void setExecutorPermission(@NotNull final Predicate<CommandSender> executorPermission) {
        this.command.setExecutorPermission(i -> executorPermission.test((CommandSender) i));
    }

    /**
     * Add default executor to define how to execute this command.
     *
     * for example :
     * <code>
     * this.addExecutor(... ,CommandArgument.ofString("example"),CommandArgument.ofString());
     * </code>
     * which means that it runs when you execute the command with "example" "xxx".
     *
     * <code>
     * this.addExecutor(...);
     * </code>
     * which means that it runs when you just execute the command without anything.
     *
     * @param executor         the executor to define this command
     * @param commandArguments the defined arguments for this executor
     * @return the Executor to define other proprieties
     */
    @NotNull
    public final top.focess.command.Command.Executor addExecutor(@NotNull final CommandExecutor executor, @NotNull final CommandArgument<?>... commandArguments) {
        return this.command.addExecutor(executor, commandArguments);
    }

    /**
     * Execute the command with special arguments
     *
     * @param sender    the executor
     * @param args      the arguments that command spilt by spaces
     * @param ioHandler the receiver
     * @return the command result
     * @see CommandLine#exec(CommandSender, String, IOHandler)
     *
     * @throws Exception the exception that occurred when executing the command
     */
    public final CommandResult execute(@NotNull final CommandSender sender, @NotNull final String[] args, @NotNull final IOHandler ioHandler) throws Exception {
        CommandResult result = this.command.execute(sender, args,ioHandler);
        if (result.isExecuted()) {
            final CommandExecutedEvent event = new CommandExecutedEvent(args, ioHandler, sender, result);
            try {
                EventManager.submit(event);
            } catch (final EventSubmitException e) {
                FocessQQ.getLogger().thrLang("exception-submit-command-executed-event", e);
            }
        }
        return result;
    }

    @NotNull
    public CommandPermission getPermission() {
        return this.command.getPermission();
    }

    /**
     * Set the default permission
     *
     * @param permission the target permission the command need
     */
    public void setPermission(final CommandPermission permission) {
        this.command.setPermission(permission);
    }

    /**
     * Used to initialize the command (the primary goal is to define the default executors)
     */
    public abstract void init();

    /**
     * Used to get help information when execute this command with wrong arguments or the executor returns {@link CommandResult#ARGS}
     *
     * @param sender the executor which need to get help information
     * @return the help information
     */
    @NotNull
    public abstract List<String> usage(CommandSender sender);

    public final void infoUsage(final CommandSender sender, final IOHandler ioHandler) {
        this.command.infoUsage(sender, ioHandler);
    }

}
