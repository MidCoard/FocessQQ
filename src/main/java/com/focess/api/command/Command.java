package com.focess.api.command;

import com.focess.api.Plugin;
import com.focess.api.util.IOHandler;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import net.mamoe.mirai.contact.MemberPermission;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public abstract class Command{


    private static final List<Command> commands = Lists.newCopyOnWriteArrayList();

    private final List<Executor> executors = Lists.newArrayList();
    private final String name;
    private Plugin plugin;

    public Plugin getPlugin() {
        return plugin;
    }

    public void unregister() {
        this.registered = false;
        commands.remove(this);
    }

    public static void unregister(Plugin plugin) {
        for (Command command:commands)
            if (command.getPlugin().equals(plugin))
                command.unregister();
    }

    public static void unregisterAll() {
        for (Command command: commands)
            command.unregister();
    }

    public String getName() {
        return name;
    }

    public List<String> getAli() {
        return ali;
    }

    private final List<String> ali;
    private boolean registered;
    private MemberPermission permission;

    public Command(final String name, final List<String> ali) {
        this.name = name;
        this.ali = ali;
        this.setPermission(MemberPermission.MEMBER);
        this.init();
    }

    public static List<Command> getCommands() {
        return commands;
    }

    public final void setPermission(MemberPermission permission) {
        this.permission = permission;
    }


    public static boolean register(Plugin plugin,final Command command) {
        for (Command c:commands)
            if (c.getName().equals(command.getName()))
                return false;
        if (plugin == null)
            return false;
        command.registered = true;
        command.plugin = plugin;
        Command.commands.add(command);
        return true;
    }

    public final Executor addExecutor(final int count, final CommandExecutor executor, final String... subCommands) {
        Executor executor1 = new Executor(count, subCommands).addExecutor(executor);
        this.executors.add(executor1);
        return executor1;
    }

    private boolean checkPermission(CommandSender sender,Executor executor){
        return sender.hasPermission(executor.permission);
    }

    public final boolean execute(final CommandSender sender, final String[] args, IOHandler ioHandler) {
        if (!this.registered)
            return true;
        if (!sender.hasPermission(permission))
            return true;
        final int amount = args.length;
        boolean flag = false;
        for (final Executor executor : this.executors) {
            if (executor.checkCount(amount) && executor.checkArgs(args)) {
                CommandResult result;
                if (this.checkPermission(sender, executor))
                    result = executor.execute(sender, Arrays.copyOfRange(args, executor.getSubCommandsSize(), args.length),ioHandler);
                else result = CommandResult.REFUSE;
                for (CommandResult r : executor.results.keySet())
                    if ((r.getPos() & result.getPos()) == 1)
                        executor.results.get(r).execute();
                flag = true;
                break;
            }
        }
        if (!flag)
            this.usage(sender,ioHandler);
        return true;
    }

    protected List<String> getCompleteLists(CommandSender sender, String cmd, String[] args) {
        return Lists.newArrayList();
    }

    public abstract void init();

    public final List<String> tabComplete(final CommandSender sender, final String cmd, final String[] args) {
        final List<String> ret = this.getCompleteLists(sender, cmd, args);
        if (args == null || args.length == 0)
            return ret;
        if (ret == null || ret.size() == 0) {
            for (final Executor executor : this.executors)
                if (checkPermission(sender,executor)&& args.length - 1 >= executor.getSubCommandsSize() && executor.checkArgs(args)) {
                    int pos = args.length - executor.getSubCommandsSize();
                    if (executor.tabCompletes.length < pos)
                        continue;
                    boolean flag = false;
                    for (int i = 0;i < pos - 1;i++)
                        if (!executor.tabCompletes[i].accept(args[i + executor.getSubCommandsSize()])) {
                            flag = true;
                            break;
                        }
                    if (!flag)
                        ret.addAll(executor.tabCompletes[pos - 1].getTabComplete(sender));
                }
            for (Executor executor:this.executors)
                if (checkPermission(sender,executor) && args.length <= executor.getSubCommandsSize() && executor.checkArgs(args,executor.getSubCommandsSize() - 1))
                    ret.add(executor.subCommands[executor.getSubCommandsSize() - 1]);
        }
        return ret.parallelStream().filter(str -> str.startsWith(args[args.length - 1]))
                    .collect(Collectors.toList());
    }

    public abstract void usage(CommandSender commandSender, IOHandler ioHandler);

    public static class Executor {
        private final int count;
        private final String[] subCommands;
        private CommandExecutor executor;
        private MemberPermission permission = MemberPermission.MEMBER;
        private TabCompleter[] tabCompletes = new TabCompleter[0];
        private Map<CommandResult,CommandResultExecutor> results = Maps.newHashMap();
        private DataConverter[] dataConverters;
        private boolean useDefaultConverter = true;

        private Executor(final int count, final String... subCommands) {
            this.subCommands = subCommands;
            this.count = count;
        }

        private Executor addExecutor(final CommandExecutor executor) {
            this.executor = executor;
            return this;
        }

        private boolean checkArgs(final String[] args) {
            return this.checkArgs(args,this.getSubCommandsSize());
        }

        private boolean checkArgs(final String[] args,int count) {
            for (int i = 0; i < count; i++)
                if (!this.subCommands[i].equals(args[i]))
                    return false;
            return true;
        }

        private boolean checkCount(final int amount) {
            return this.subCommands.length + this.count == amount;
        }

        private CommandResult execute(final CommandSender sender, final String[] args, IOHandler ioHandler) {
            if (this.useDefaultConverter) {
                List<DataConverter<?>> dataConverters = Lists.newArrayList();
                for (TabCompleter tabCompleter:this.tabCompletes)
                    dataConverters.add(tabCompleter);
                int size = args.length - dataConverters.size();
                for (int i = 0;i<size;i++)
                    dataConverters.add(DataConverter.DEFAULT_DATA_CONVERTER);
                this.dataConverters = dataConverters.toArray(new DataConverter[0]);
            }
            else if (this.dataConverters.length < args.length) {
                List<DataConverter<?>> dataConverters = Lists.newArrayList(this.dataConverters);
                for (int i = 0;i<args.length - this.dataConverters.length;i++)
                    dataConverters.add(DataConverter.DEFAULT_DATA_CONVERTER);
                this.dataConverters = dataConverters.toArray(new DataConverter[0]);
            }
            DataCollection dataCollection = new DataCollection(args.length);
            for (int i = 0;i<args.length;i++)
                if (!this.dataConverters[i].put(dataCollection,args[i]))
                    return CommandResult.ARGS;
            dataCollection.flip();
            return this.executor.execute(sender, dataCollection,ioHandler);
        }

        private int getSubCommandsSize() {
            return this.subCommands.length;
        }


        public Executor addPermission(MemberPermission permission) {
            this.permission = permission;
            return this;
        }

        public Executor addTabComplete(TabCompleter... tabCompleters) {
            this.tabCompletes = tabCompleters;
            return this;
        }

        public Executor addCommandResult(CommandResult result, CommandResultExecutor executor) {
            results.put(result,executor);
            return this;
        }

        public Executor addDataConverter(DataConverter...dataConverters) {
            this.dataConverters = dataConverters;
            this.useDefaultConverter = false;
            return this;
        }

        public Executor setUseDefaultConverter(boolean flag) {
            this.useDefaultConverter = flag;
            return this;
        }
    }
}
