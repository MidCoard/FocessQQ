package top.focess.qq.api.command;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import top.focess.qq.FocessQQ;
import top.focess.qq.api.event.EventManager;
import top.focess.qq.api.event.command.CommandExecutedEvent;
import top.focess.qq.api.event.EventSubmitException;
import top.focess.qq.api.plugin.Plugin;
import top.focess.qq.api.plugin.PluginType;
import top.focess.qq.api.util.IOHandler;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

/**
 * Represent a Plugin class that can execute. Just like we use the terminal, we could use it to executing some commands. This is an important way to interact with Mirai QQ Bot.
 * You should declare {@link CommandType} to this class ,or you should register it with your plugin manually.
 */
public abstract class Command {


    private static final Map<String,Command> COMMANDS_MAP = Maps.newConcurrentMap();

    private final List<Executor> executors = Lists.newCopyOnWriteArrayList();

    /**
     * The name of the command
     */
    private String name;
    /**
     * The aliases of the command
     */
    private List<String> aliases;

    /**
     * The plugin the command belongs to
     */
    private Plugin plugin;

    /**
     * Indicate whether the command is registered or not
     */
    private boolean registered;

    /**
     * The MiraiPermission of the command
     */
    private CommandPermission permission;

    /**
     * The executor check predicate
     */
    private Predicate<CommandSender> executorPermission;

    /**
     * Indicate {@link Command#init()} is called
     */
    private boolean initialize;

    /**
     * Instance a <code>Command</code> Class with special name and aliases.
     *
     * @param name the name of the command
     * @param aliases the aliases of the command
     * @throws CommandLoadException if there is any exception thrown in the initializing process
     */
    public Command(@NotNull final String name, @NotNull final String... aliases) {
        this.name = name;
        this.aliases = Lists.newArrayList(aliases);
        this.permission = CommandPermission.MEMBER;
        this.executorPermission = i -> true;
        try {
            this.init();
        } catch (final Exception e) {
            throw new CommandLoadException(this.getClass(),e);
        }
        this.initialize = true;
    }

    /**
     * Provide a constructor to help {@link PluginType} design.
     * Never instance it! It will be instanced when this class is loaded automatically.
     */
    protected Command() {
        this.permission = CommandPermission.MEMBER;
        this.executorPermission = i -> true;
    }

    public void setExecutorPermission(@NotNull final Predicate<CommandSender> executorPermission) {
        this.executorPermission = executorPermission;
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
     * @return All commands as a <code>List</code>
     */
    public static List<Command> getCommands() {
        return Lists.newArrayList(COMMANDS_MAP.values());
    }


    /**
     * Register the command
     *
     * @param plugin the plugin the command belongs to
     * @param command the command that need to be registered
     * @throws CommandDuplicateException if the command name already exists in the registered commands
     */
    public static void register(@NotNull final Plugin plugin, @NotNull final Command command) {
        if (command.name == null)
            throw new IllegalStateException("CommandType does not contain name or the constructor does not super name");
        if (COMMANDS_MAP.containsKey(command.getName()))
            throw new CommandDuplicateException(command.getName());
        command.registered = true;
        command.plugin = plugin;
        COMMANDS_MAP.put(command.getName(),command);
    }

    public boolean isRegistered() {
        return this.registered;
    }

    @NotNull
    public Plugin getPlugin() {
        return this.plugin;
    }

    /**
     * Unregister this command
     */
    public void unregister() {
        this.registered = false;
        this.executors.clear();
        COMMANDS_MAP.remove(this.getName());
    }

    @NotNull
    public String getName() {
        return this.name;
    }

    @NotNull
    public List<String> getAliases() {
        return this.aliases;
    }

    public Predicate<CommandSender> getExecutorPermission() {
        return this.executorPermission;
    }

    /**
     * Add default executor to define how to execute this command.
     *
     * for example :
     * <code>
     *     this.addExecutor(1, ... ,"example");
     * </code>
     * which means that it runs when you execute the command with "example" "xxx".
     *
     * <code>
     *     this.addExecutor(0, ...);
     * </code>
     * which means that it runs when you just execute the command without anything.
     *
     * @param executor the executor to define this command
     * @param commandArguments the defined arguments for this executor
     * @return the Executor to define other proprieties
     */
    @NotNull
    public final Executor addExecutor(@NotNull final CommandExecutor executor, @NotNull final CommandArgument<?>... commandArguments) {
        final Executor executor1 = new Executor(executor,this.executorPermission,this,commandArguments);
        this.executors.add(executor1);
        return executor1;
    }

    /**
     * Execute the command with special arguments
     * @see CommandLine#exec(CommandSender, String, IOHandler)
     *
     * @param sender the executor
     * @param args the arguments that command spilt by spaces
     * @param ioHandler the receiver
     * @return the command result
     */
    public final CommandResult execute(@NotNull final CommandSender sender, @NotNull final String[] args,@NotNull final IOHandler ioHandler) {
        if (!this.isRegistered())
            return CommandResult.COMMAND_REFUSED;
        if (!sender.hasPermission(this.getPermission()))
            return CommandResult.COMMAND_REFUSED;
        boolean flag = false;
        CommandResult result = CommandResult.NONE;
        for (final Executor executor : this.executors)
            if (sender.hasPermission(executor.permission)) {
                final DataCollection dataCollection;
                if ((dataCollection = executor.check(args)) != null) {
                    try {
                        result = executor.execute(sender, dataCollection, ioHandler);
                    } catch (final Exception e) {
                        result = CommandResult.REFUSE;
                        FocessQQ.getLogger().thrLang("exception-command-execute", e);
                        ioHandler.outputLang("command-execute-exception",e.getMessage());
                    }
                    for (final CommandResult r : executor.results.keySet())
                        if ((r.getValue() & result.getValue()) != 0)
                            executor.results.get(r).execute(result);
                    final CommandExecutedEvent event = new CommandExecutedEvent(executor, args, ioHandler, sender, result);
                    try {
                        EventManager.submit(event);
                    } catch (final EventSubmitException e) {
                        FocessQQ.getLogger().thrLang("exception-submit-command-executed-event", e);
                    }
                    flag = true;
                    break;
                }
            }
        if (this.executorPermission.test(sender) && (!flag || result == CommandResult.ARGS)) {
            this.infoUsage(sender, ioHandler);
            return CommandResult.ARGS;
        }
        return result;
    }

    @NotNull
    public CommandPermission getPermission() {
        return this.permission;
    }

    /**
     * Used to initialize the command (the primary goal is to define the default executors)
     *
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
        final List<String> usage = this.usage(sender);
        int pos = 0;
        final int targetPos = 7;
        StringBuilder stringBuilder = null;
        while (pos != usage.size()) {
            if (pos % targetPos == 0) {
                if (stringBuilder != null)
                    ioHandler.output(stringBuilder.toString());
                stringBuilder = new StringBuilder(usage.get(pos));
            } else stringBuilder.append('\n').append(usage.get(pos));
            pos++;
        }
        if (stringBuilder != null)
            ioHandler.output(stringBuilder.toString());
    }

    public boolean isInitialize() {
        return this.initialize;
    }

    /**
     * Set the default permission
     *
     * @param permission the target permission the command need
     */
    public void setPermission(final CommandPermission permission) {
        this.permission = permission;
    }

    /**
     * This class is used to help define the executor of certain command.
     * There is some special methods used to give more details of this executor.
     *
     */
    public static class Executor {
        private final Map<CommandResult, CommandResultExecutor> results = Maps.newHashMap();
        private final CommandExecutor executor;
        private final CommandArgument<?>[] commandArguments;
        private CommandPermission permission = CommandPermission.MEMBER;
        private Predicate<CommandSender> executorPermission;
        private final Command command;
        private final int nullableCommandArguments;

        private Executor(final CommandExecutor executor, final Predicate<CommandSender> executorPermission, final Command command, final CommandArgument<?>[] commandArguments) {
            this.executor = executor;
            this.executorPermission = executorPermission;
            this.command = command;
            this.commandArguments = commandArguments;
            this.nullableCommandArguments = (int) Arrays.stream(commandArguments).filter(CommandArgument::isNullable).count();
        }

        private CommandResult execute(final CommandSender sender, final DataCollection dataCollection, final IOHandler ioHandler) {
            if (!this.executorPermission.test(sender))
                return CommandResult.REFUSE;
            return this.executor.execute(sender, dataCollection, ioHandler);
        }


        /**
         * Set the executor Permission
         * (Only if the CommandSender has the command that this executor belongs to and this executor's permissions, this executor runs)
         *
         * @param permission the executor Mirai Permission
         * @return the Executor itself
         */
        @NotNull
        public Executor setPermission(@NotNull final CommandPermission permission) {
            this.permission = permission;
            return this;
        }


        /**
         * Set the executor of the special CommandResult after executing this Executor
         *
         * @param result the target CommandResult
         * @param executor the executor of the special CommandResult
         * @return the Executor itself
         */
        @NotNull
        public Executor addCommandResultExecutor(@NotNull final CommandResult result, @NotNull final CommandResultExecutor executor) {
            this.results.put(result, executor);
            return this;
        }

        /**
         * Set the executor permission check for this Executor
         * When execute this Executor, it will check {@link Command#executorPermission} and the executorPermission
         *
         * @param executorPermission the executor permission check for this Executor
         * @return the Executor self
         */
        @NotNull
        public Executor setExecutorPermission(@NotNull final Predicate<CommandSender> executorPermission) {
            this.executorPermission = this.executorPermission.and(executorPermission);
            return this;
        }

        /**
         * Remove the executor permission check for this Executor
         *
         * @return the Executor self
         */
        @NotNull
        public Executor removeExecutorPermission() {
            this.executorPermission = i -> true;
            return this;
        }

        /**
         * Set the executor permission check for this Executor
         * When execute this Executor, it will only check the executorPermission
         *
         * @param executorPermission the executor permission check for this Executor
         * @return the Executor self
         */
        @NotNull
        public Executor overrideExecutorPermission(@NotNull final Predicate<CommandSender> executorPermission) {
            this.executorPermission = executorPermission;
            return this;
        }

        /**
         * Get the command this Executor belongs to
         *
         * @return the command this Executor belongs to
         */
        public Command getCommand() {
            return this.command;
        }

        @Nullable
        private DataCollection check(final String[] args) {
            if (args.length > this.commandArguments.length)
                return null;
            if (args.length < this.commandArguments.length - this.nullableCommandArguments)
                return null;
            final List<CommandArgument<?>> commandArgumentList = Lists.newArrayList();
            final boolean ret = this.dfsCheck(args,0,0,this.commandArguments.length - args.length,commandArgumentList);
            if (!ret)
                return null;
            final DataCollection dataCollection = new DataCollection(Arrays.stream(this.commandArguments).map(CommandArgument::getDataConverter).toArray(DataConverter[]::new));
            for (int i = 0; i < args.length; i++)
                commandArgumentList.get(i).put(dataCollection, args[i]);
            dataCollection.flip();
            return dataCollection;
        }

        private boolean dfsCheck(final String[] args, final int indexOfArgs, final int index, final int nullableCommandArguments, final List<CommandArgument<?>> commandArgumentList) {
            if (indexOfArgs == args.length)
                return true;
            if (this.commandArguments[index].isNullable() && nullableCommandArguments > 0) {
                final boolean ret = this.dfsCheck(args, indexOfArgs, index + 1, nullableCommandArguments - 1, commandArgumentList);
                if (ret)
                    return true;
            }
            if (this.commandArguments[index].accept(args[indexOfArgs])) {
                commandArgumentList.add(this.commandArguments[index]);
                final boolean ret = this.dfsCheck(args,indexOfArgs + 1,index + 1,nullableCommandArguments,commandArgumentList);
                if (ret)
                    return true;
                commandArgumentList.remove(commandArgumentList.size() - 1);
            }
            return false;
        }
    }
}
