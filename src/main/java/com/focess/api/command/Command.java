package com.focess.api.command;

import com.focess.Main;
import com.focess.api.plugin.Plugin;
import com.focess.api.event.EventManager;
import com.focess.api.event.command.CommandExecutedEvent;
import com.focess.api.exceptions.CommandDuplicateException;
import com.focess.api.exceptions.CommandLoadException;
import com.focess.api.exceptions.EventSubmitException;
import com.focess.api.plugin.PluginType;
import com.focess.api.util.IOHandler;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import net.mamoe.mirai.contact.MemberPermission;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

/**
 * Represent a Plugin class that can execute. Just like we use the terminal, we could use it to executing some commands. This is an important way to interact with Mirai QQ Bot.
 * You should declare {@link CommandType} to this class ,or you should register it with your plugin manually.
 */
public abstract class Command {


    private static final Map<String,Command> COMMANDS_MAP = Maps.newConcurrentMap();

    private final List<Executor> executors = Lists.newArrayList();

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
    private boolean initialize = false;

    /**
     * Instance a <code>Command</code> Class with special name and aliases.
     *
     * @param name the name of the command
     * @param aliases the aliases of the command
     * @throws CommandLoadException if there is any exception thrown in the initializing process
     */
    public Command(final @NotNull String name, final @NotNull String... aliases) {
        this.name = name;
        this.aliases = Lists.newArrayList(aliases);
        this.permission = CommandPermission.MEMBER;
        this.executorPermission = i -> true;
        try {
            this.init();
        } catch (Exception e) {
            throw new CommandLoadException(this.getClass(),e);
        }
        initialize = true;
    }

    /**
     * Provide a constructor to help {@link PluginType} design.
     * Never instance it! It will be instanced when this class is loaded automatically.
     */
    protected Command() {
    }

    public void setExecutorPermission(@NotNull Predicate<CommandSender> executorPermission) {
        this.executorPermission = executorPermission;
    }

    /**
     * Unregister all commands in the plugin
     *
     * @param plugin the plugin that the commands that need to be unregistered belongs to
     */
    public static void unregister(Plugin plugin) {
        for (Command command : COMMANDS_MAP.values())
            if (command.getPlugin().equals(plugin))
                command.unregister();
    }

    /**
     * Unregister all commands
     */
    public static void unregisterAll() {
        for (Command command : COMMANDS_MAP.values())
            command.unregister();
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
     * @throws com.focess.api.exceptions.CommandDuplicateException if the command name already exists in the registered commands
     */
    public static void register(@NotNull Plugin plugin, @NotNull final Command command) {
        //todo if no name throw CommandLoadException
        if (COMMANDS_MAP.containsKey(command.getName()))
            throw new CommandDuplicateException(command.getName());
        command.registered = true;
        command.plugin = plugin;
        Command.COMMANDS_MAP.put(command.getName(),command);
    }

    public boolean isRegistered() {
        return this.registered;
    }

    @NotNull
    public Plugin getPlugin() {
        return plugin;
    }

    /**
     * Unregister this command
     */
    public void unregister() {
        this.registered = false;
        COMMANDS_MAP.remove(this.getName());
    }

    @NotNull
    public String getName() {
        return name;
    }

    @NotNull
    public List<String> getAliases() {
        return aliases;
    }

    public Predicate<CommandSender> getExecutorPermission() {
        return executorPermission;
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
     * @param count the arguments' length that you need
     * @param executor the executor to define this command
     * @param subCommands the known arguments for this executor
     * @return the Executor to define other proprieties
     */
    @NotNull
    public final Executor addExecutor(final int count, final @NotNull CommandExecutor executor, final String... subCommands) {
        Executor executor1 = new Executor(count, executor,this.executorPermission,this,subCommands);
        this.executors.add(executor1);
        return executor1;
    }

    /**
     * Execute the command with special arguments
     * @see com.focess.Main.CommandLine#exec(CommandSender, String, IOHandler)
     *
     * @param sender the executor
     * @param args the arguments that command spilt by spaces
     * @param ioHandler the receiver
     * @return an Executor that help to define the executor of this command
     */
    public final boolean execute(@NotNull final CommandSender sender, @NotNull final String[] args,@NotNull IOHandler ioHandler) {
        if (!this.isRegistered())
            return false;
        if (!sender.hasPermission(this.getPermission()))
            return false;
        final int amount = args.length;
        boolean flag = false;
        CommandResult result = CommandResult.NONE;
        for (final Executor executor : this.executors)
            if (executor.checkCount(amount) && executor.checkArgs(args)) {
                if (sender.hasPermission(executor.permission))
                    result = executor.execute(sender, Arrays.copyOfRange(args, executor.getSubCommandsSize(), args.length), ioHandler);
                else result = CommandResult.REFUSE;
                for (CommandResult r : executor.results.keySet())
                    if ((r.getValue() & result.getValue()) != 0)
                        executor.results.get(r).execute(result);
                CommandExecutedEvent event = new CommandExecutedEvent(executor,args,ioHandler,sender,result);
                try {
                    EventManager.submit(event);
                } catch (EventSubmitException e) {
                    Main.getLogger().thr("Submit Command Executed Exception",e);
                }
                flag = true;
            }
        if ((!flag && this.executorPermission.test(sender)) || result == CommandResult.ARGS)
            this.usage(sender, ioHandler);
        return true;
    }

    @NotNull
    public CommandPermission getPermission() {
        return this.permission;
    }

    /**
     * Set the default permission
     *
     * @param permission the target permission the command need
     */
    @Deprecated
    public final void setPermission(@NotNull MemberPermission permission) {
        this.permission = CommandPermission.toCommandPermission(permission);
    }

    /**
     * Used to initialize the command (the primary goal is to define the default executors)
     *
     */
    public abstract void init();

    /**
     * Used to print help information when execute this command with wrong arguments or the executor returns {@link CommandResult#ARGS}
     *
     * @param sender the executor which need to print help information
     * @param ioHandler the receiver which need to print help information
     */
    public abstract void usage(CommandSender sender, IOHandler ioHandler);

    public boolean isInitialize() {
        return initialize;
    }

    /**
     * Set the default permission
     *
     * @param permission the target permission the command need
     */
    public void setPermission(CommandPermission permission) {
        this.permission = permission;
    }

    /**
     * This class is used to help define the executor of certain command.
     * There is some special methods used to give more details of this executor.
     *
     */
    public static class Executor {
        private final int count;
        private final String[] subCommands;
        private final Map<CommandResult, CommandResultExecutor> results = Maps.newHashMap();
        private final CommandExecutor executor;
        private CommandPermission permission = CommandPermission.MEMBER;
        private DataConverter<?>[] dataConverters;
        private boolean useDefaultConverter = true;
        private Predicate<CommandSender> executorPermission;
        private final Command command;

        private Executor(final int count,final CommandExecutor executor,final Predicate<CommandSender> executorPermission,final Command command,final String... subCommands) {
            this.subCommands = subCommands;
            this.count = count;
            this.executor = executor;
            this.executorPermission = executorPermission;
            this.command = command;
        }

        private boolean checkArgs(final String[] args) {
            return this.checkArgs(args, this.getSubCommandsSize());
        }

        private boolean checkArgs(final String[] args, int count) {
            for (int i = 0; i < count; i++)
                if (!this.subCommands[i].equals(args[i]))
                    return false;
            return true;
        }

        private boolean checkCount(final int amount) {
            return this.subCommands.length + this.count == amount;
        }

        private CommandResult execute(final CommandSender sender, final String[] args, IOHandler ioHandler) {
            if (!this.executorPermission.test(sender))
                return CommandResult.REFUSE;
            if (this.useDefaultConverter)
                this.dataConverters = Collections.nCopies(args.length,DataConverter.DEFAULT_DATA_CONVERTER).toArray(new DataConverter[0]);
            else if (this.dataConverters.length < args.length) {
                List<DataConverter<?>> dataConverters = Lists.newArrayList(this.dataConverters);
                for (int i = 0; i < args.length - this.dataConverters.length; i++)
                    dataConverters.add(DataConverter.DEFAULT_DATA_CONVERTER);
                this.dataConverters = dataConverters.toArray(new DataConverter[0]);
            }
            DataCollection dataCollection = new DataCollection(args.length);
            for (int i = 0; i < args.length; i++)
                if (!this.dataConverters[i].put(dataCollection, args[i]))
                    return CommandResult.ARGS;
            dataCollection.flip();
            return this.executor.execute(sender, dataCollection, ioHandler);
        }

        private int getSubCommandsSize() {
            return this.subCommands.length;
        }

        /**
         * Set the executor Mirai Permission
         * (Only if the CommandSender has the command that this executor belongs to and this executor's permissions, this executor runs)
         *
         * @param permission the executor Mirai Permission
         * @return the Executor itself
         */
        @NotNull
        @Deprecated
        public Executor setPermission(@NotNull MemberPermission permission) {
            this.permission = CommandPermission.toCommandPermission(permission);
            return this;
        }

        /**
         * Set the executor Mirai Permission
         * (Only if the CommandSender has the command that this executor belongs to and this executor's permissions, this executor runs)
         *
         * @param permission the executor Mirai Permission
         * @return the Executor itself
         */
        @NotNull
        public Executor setPermission(@NotNull CommandPermission permission) {
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
        public Executor setCommandResultExecutors(@NotNull CommandResult result,@NotNull CommandResultExecutor executor) {
            results.put(result, executor);
            return this;
        }

        /**
         * Set the DataConverters for the arguments.
         * Only if {@link Executor#setUseDefaultConverter(boolean)} is set false, this method will influence the DataCollection parser.
         * @see Executor#setUseDefaultConverter(boolean)
         *
         * @param dataConverters used to parser arguments this executor needs.
         * @return the Executor itself
         */
        @NotNull
        public Executor setDataConverters(@NotNull DataConverter<?>... dataConverters) {
            this.dataConverters = dataConverters;
            this.useDefaultConverter = false;
            return this;
        }

        /**
         * Set whether force use the {@link DataConverter#DEFAULT_DATA_CONVERTER} or not.
         * If this is set true, the influence by {@link Executor#setDataConverters(DataConverter[])} is ignored.
         * @see Executor#setDataConverters(DataConverter[])
         *
         * @param flag true force use the {@link DataConverter#DEFAULT_DATA_CONVERTER}, false ignore it
         * @return the Executor itself
         */
        @NotNull
        public Executor setUseDefaultConverter(boolean flag) {
            this.useDefaultConverter = flag;
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
        public Executor setExecutorPermission(@NotNull Predicate<CommandSender> executorPermission) {
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
        public Executor overrideExecutorPermission(@NotNull Predicate<CommandSender> executorPermission) {
            this.executorPermission = executorPermission;
            return this;
        }

        /**
         * Get the command this Executor belongs to
         *
         * @return the command this Executor belongs to
         */
        public Command getCommand() {
            return command;
        }
    }
}
