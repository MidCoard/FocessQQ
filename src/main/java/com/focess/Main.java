package com.focess;

import com.focess.api.Plugin;
import com.focess.api.command.Command;
import com.focess.api.command.CommandSender;
import com.focess.api.util.IOHandler;
import com.focess.commands.*;

import com.focess.util.Pair;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Queues;
import kotlin.coroutines.Continuation;
import kotlin.coroutines.CoroutineContext;
import net.mamoe.mirai.Bot;
import net.mamoe.mirai.BotFactoryJvm;
import net.mamoe.mirai.event.EventHandler;
import net.mamoe.mirai.event.Events;
import net.mamoe.mirai.event.SimpleListenerHost;
import net.mamoe.mirai.message.FriendMessageEvent;
import net.mamoe.mirai.message.GroupMessageEvent;
import net.mamoe.mirai.message.data.SingleMessage;
import net.mamoe.mirai.utils.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.imageio.stream.FileImageOutputStream;
import java.io.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

public class Main{

    private static final Map<CommandSender,Queue<Pair<IOHandler, Boolean>>> quests = Maps.newHashMap();

    public static void registerIOHandler(IOHandler ioHandler, CommandSender commandSender, boolean flag) {
        quests.compute(commandSender,(k,v)->{
            if (v == null)
                v = Queues.newConcurrentLinkedQueue();
            v.offer(Pair.of(ioHandler,flag));
            return v;
        });
    }

    public static Scanner scanner;

    public static Bot getBot() {
        return bot;
    }

    public static class MainPlugin extends Plugin {

        private static Map<String,Object> properties;

        public MainPlugin() {
            super("Main");
        }

        public static Map<String, Object> getProperties() {
            return properties;
        }

        @Override
        public void enable() {
            properties = getConfig().getValues();
            if (properties == null)
                properties = Maps.newHashMap();
        }

        @Override
        public void disable() {
            getConfig().save(getConfigFile());
        }

    }

    private final static MainPlugin MAIN_PLUGIN = new MainPlugin();

    private static long user = 3418652527L;

    private static String password = "asnbot371237";

    @Deprecated
    public static Bot bot;

    public static void main(String[] args) {
        if (args.length == 2) {
            try {
                user = Long.parseLong(args[0]);
                password = args[1];
            } catch (Exception ignored) {}
        }
        LoadCommand.loadPlugin(MAIN_PLUGIN);
        init();
        scanner = new Scanner(System.in);
        BotConfiguration configuration = BotConfiguration.getDefault();
        configuration.fileBasedDeviceInfo();
        configuration.setLoginSolver(new LoginSolver() {
            @Nullable
            @Override
            public Object onSolvePicCaptcha(@NotNull Bot bot, @NotNull byte[] bytes, @NotNull Continuation<? super String> continuation) {
                try {
                    FileImageOutputStream outputStream = new FileImageOutputStream(new File("captcha.jpg"));
                    outputStream.write(bytes);
                    outputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return scanner.nextLine();
            }

            @Nullable
            @Override
            public Object onSolveSliderCaptcha(@NotNull Bot bot, @NotNull String s, @NotNull Continuation<? super String> continuation) {
                IOHandler.IO_HANDLER.output(s);
                scanner.nextLine();
                return null;
            }

            @Nullable
            @Override
            public Object onSolveUnsafeDeviceLoginVerify(@NotNull Bot bot, @NotNull String s, @NotNull Continuation<? super String> continuation) {
                IOHandler.IO_HANDLER.output(s);
                scanner.nextLine();
                return null;
            }
        });
        configuration.setBotLoggerSupplier((b) -> new MiraiLoggerWithSwitch(Utils.getDefaultLogger().invoke(""), false));
        bot = BotFactoryJvm.newBot(user, password, configuration);
        getBot().login();
        Events.registerEvents(new SimpleListenerHost() {
            @Override
            public void handleException(@NotNull CoroutineContext context, @NotNull Throwable exception) {
                super.handleException(context, exception);
            }

            @EventHandler(ignoreCancelled = true)
            public void onMessage(GroupMessageEvent event) {
                IOHandler.IO_HANDLER.output("G--------" + event.getGroup().getName() + ":"  + event.getGroup().getId() + "--------");
                IOHandler.IO_HANDLER.output("Permission: " + event.getPermission());
                IOHandler.IO_HANDLER.output("NameCard: " + event.getSender().getNameCard());
                IOHandler.IO_HANDLER.output("ID: " + event.getSender().getId());
                IOHandler.IO_HANDLER.output("Message: " + event.getMessage().contentToString());
                IOHandler.IO_HANDLER.output("MessageChain: " );
                event.getMessage().forEach(SingleMessage::contentToString);
                IOHandler.IO_HANDLER.output("RawMessage: " + event.getMessage());
                IOHandler.IO_HANDLER.output("RawMessageChain: ");
                event.getMessage().forEach(System.out::println);
                CommandSender now = CommandSender.getCommandSender(new CommandSender.MemberOrConsoleOrFriend(event.getSender()));
                AtomicBoolean flag = new AtomicBoolean(false);
                quests.compute(now,(k,v)->{
                    if (v !=null && !v.isEmpty()) {
                        Pair<IOHandler,Boolean> element = v.poll();
                        if (element.getValue())
                            element.getKey().handle(event.getMessage().contentToString());
                        else element.getKey().handle(event.getMessage().toString());
                        flag.set(true);
                    }
                    return v;
                });
                if (!flag.get())
                    CommandLine.exec(now,event.getMessage().contentToString(),IOHandler.getIoHandlerByCommandSender(now));
            }

            @EventHandler(ignoreCancelled = true)
            public void onMessage(FriendMessageEvent event) {
                IOHandler.IO_HANDLER.output("F--------" + event.getFriend().getNick()  + ":" + event.getFriend().getId()+ "--------");
                IOHandler.IO_HANDLER.output("ID: " + event.getSender().getId());
                IOHandler.IO_HANDLER.output("Message: " + event.getMessage().contentToString());
                IOHandler.IO_HANDLER.output("MessageChain: " );
                event.getMessage().forEach(SingleMessage::contentToString);
                IOHandler.IO_HANDLER.output("RawMessage: " + event.getMessage());
                IOHandler.IO_HANDLER.output("RawMessageChain: ");
                event.getMessage().forEach(System.out::println);
                CommandSender now = CommandSender.getCommandSender(new CommandSender.MemberOrConsoleOrFriend(event.getSender()));
                AtomicBoolean flag = new AtomicBoolean(false);
                quests.compute(now,(k,v)->{
                    if (v !=null && !v.isEmpty()) {
                        Pair<IOHandler,Boolean> element = v.poll();
                        if (element.getValue())
                            element.getKey().handle(event.getMessage().contentToString());
                        else element.getKey().handle(event.getMessage().toString());
                        flag.set(true);
                    }
                    return v;
                });
                if (!flag.get())
                    CommandLine.exec(now,event.getMessage().contentToString(),IOHandler.getIoHandlerByCommandSender(now));
            }

        });
        while (IOHandler.getIoHandler().hasInput(true))
            CommandLine.exec(CommandSender.getCommandSender(new CommandSender.MemberOrConsoleOrFriend()),IOHandler.IO_HANDLER.input(), IOHandler.IO_HANDLER);
        scanner.close();
    }

    private static void init() {
        Command.register(MAIN_PLUGIN,new LoadCommand());
        Command.register(MAIN_PLUGIN,new UnloadCommand());
        Command.register(MAIN_PLUGIN,new StopCommand());
        File plugins = new File("plugins");
        if (plugins.exists())
            for (File file: Objects.requireNonNull(plugins.listFiles(file -> file.getName().endsWith(".jar"))))
                CommandLine.exec(CommandSender.CONSOLE,"load plugins/" + file.getName(),IOHandler.IO_HANDLER);
    }

    public static void exit() {
        for (Plugin plugin:LoadCommand.getPlugins())
            if(!plugin.equals(MAIN_PLUGIN))
                CommandLine.exec(CommandSender.CONSOLE,"unload " + plugin.getName(),IOHandler.IO_HANDLER);
        LoadCommand.disablePlugin(MAIN_PLUGIN);
        System.exit(0);
    }

    public static class CommandLine {

        public static void exec(CommandSender sender, String command, IOHandler ioHandler) {
            IOHandler.IO_HANDLER.output(sender + " EXEC: \"" + command + "\"");
            List<String> args = Lists.newArrayList();
            StringBuilder stringBuilder = new StringBuilder();
            boolean stack = false;
            for (char c:command.toCharArray())
                if(c == ' ' && !stack){
                    args.add(stringBuilder.toString());
                    stringBuilder.delete(0,stringBuilder.length());
                }
                else {
                    stringBuilder.append(c);
                    if (c == '"')
                        stack = !stack;
                }
            args.add(stringBuilder.toString());
            String name = args.get(0);
            args.remove(0);
            exec1(sender,name,args.toArray(new String[0]),ioHandler);
        }

        private static void exec1(CommandSender sender, String command, String[] args,IOHandler ioHandler) {
            for (Command com: Command.getCommands())
                if (com.getAli().contains(command) || com.getName().equals(command))
                    com.execute(sender,args,ioHandler);
        }
    }

}
