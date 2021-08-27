package com.focess;

import com.focess.api.Plugin;
import com.focess.api.command.Command;
import com.focess.api.command.CommandSender;
import com.focess.api.util.IOHandler;
import com.focess.commands.LoadCommand;
import com.focess.commands.StopCommand;
import com.focess.commands.UnloadCommand;
import com.focess.util.Pair;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Queues;
import kotlin.coroutines.Continuation;
import net.mamoe.mirai.Bot;
import net.mamoe.mirai.BotFactory;
import net.mamoe.mirai.contact.Friend;
import net.mamoe.mirai.event.Listener;
import net.mamoe.mirai.event.events.FriendMessageEvent;
import net.mamoe.mirai.event.events.GroupMessageEvent;
import net.mamoe.mirai.utils.BotConfiguration;
import net.mamoe.mirai.utils.LoginSolver;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.imageio.stream.FileImageOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

public class Main {

    private static final Map<CommandSender, Queue<Pair<IOHandler, Boolean>>> quests = Maps.newHashMap();
    private static MainPlugin MAIN_PLUGIN;
    private static final long AUTHOR_USER = 2624646185L;
    @Deprecated
    public static Scanner scanner;
    @Deprecated
    public static Bot bot;
    private static boolean isRunning = false;
    private static Listener<GroupMessageEvent> groupMessageEventListener;
    private static Listener<FriendMessageEvent> friendMessageEventListener;
    private static long user;
    private static String password;
    private static final Thread CONSOLE_THREAD = new Thread(()->{
        while (IOHandler.getIoHandler().hasInput())
            try {
                CommandLine.exec(IOHandler.getIoHandler().input());
            } catch (Exception e) {
                e.printStackTrace();
            }
    });
    private static boolean debug = false;

    public static void registerIOHandler(IOHandler ioHandler, CommandSender commandSender, boolean flag) {
        quests.compute(commandSender, (k, v) -> {
            if (v == null)
                v = Queues.newConcurrentLinkedQueue();
            v.offer(Pair.of(ioHandler, flag));
            return v;
        });
    }

    public static Scanner getScanner() {
        return scanner;
    }

    public static Bot getBot() {
        return bot;
    }

    public static void relogin() {
        bot.close();
        bot.login();
    }

    public static MainPlugin getMainPlugin() {
        return MAIN_PLUGIN;
    }

    public static long getUser() {
        return user;
    }

    public static long getAuthorUser() {
        return AUTHOR_USER;
    }

    public static Friend getAuthor() {
        return getBot().getFriend(getAuthorUser());
    }

    private static void updateMessage(CommandSender now, String content, String valueOf, AtomicBoolean flag) {
        quests.compute(now, (k, v) -> {
            if (v != null && !v.isEmpty()) {
                Pair<IOHandler, Boolean> element = v.poll();
                if (element.getValue())
                    element.getKey().handle(content);
                else element.getKey().handle(valueOf);
                flag.set(true);
            }
            return v;
        });
    }

    private static void requestQQ() {
        try {
            IOHandler.getIoHandler().output("please input your QQ user number:");
            user = scanner.nextLong();
            IOHandler.getIoHandler().output("please input your QQ password:");
            password = scanner.next();
        } catch (Exception e) {
            requestQQ();
        }
    }

    public static void main(String[] args) {
        scanner = new Scanner(System.in);
        if (args.length == 2) {
            try {
                user = Long.parseLong(args[0]);
                password = args[1];
            } catch (Exception ignored) {
                requestQQ();
            }
        } else requestQQ();
        CONSOLE_THREAD.start();
        try {
            MAIN_PLUGIN = LoadCommand.loadPlugin(MainPlugin.class);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void exit() {
        LoadCommand.disablePlugin(MAIN_PLUGIN);
    }

    public static void setDebug(boolean debug) {
        Main.debug = debug;
    }

    public final static class MainPlugin extends Plugin {

        private static Map<String, Object> properties;

        public MainPlugin() {
            super("MainPlugin");
            if (isRunning)
                Main.exit();
        }

        public static Map<String, Object> getProperties() {
            return properties;
        }

        @Override
        public void enable() {
            isRunning = true;
            Command.register(this, new LoadCommand());
            Command.register(this, new UnloadCommand());
            Command.register(this, new StopCommand());
            BotConfiguration configuration = BotConfiguration.getDefault();
            configuration.setProtocol(BotConfiguration.MiraiProtocol.ANDROID_PAD);
            configuration.fileBasedDeviceInfo();
            configuration.setLoginSolver(new LoginSolver() {
                @Nullable
                @Override
                public Object onSolvePicCaptcha(@NotNull Bot bot, byte[] bytes, @NotNull Continuation<? super String> continuation) {
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
                    IOHandler.getIoHandler().output(s);
                    scanner.nextLine();
                    return null;
                }

                @Nullable
                @Override
                public Object onSolveUnsafeDeviceLoginVerify(@NotNull Bot bot, @NotNull String s, @NotNull Continuation<? super String> continuation) {
                    IOHandler.getIoHandler().output(s);
                    scanner.nextLine();
                    return null;
                }
            });
            bot = BotFactory.INSTANCE.newBot(user, password, configuration);
            getBot().login();
            groupMessageEventListener = bot.getEventChannel().subscribeAlways(GroupMessageEvent.class, event -> {
                if (debug) {
                    IOHandler.getIoHandler().output(String.format("%s(%d,%s) in %s(%d): %s",event.getSender().getNameCard(),event.getSender().getId(),event.getPermission(),event.getGroup().getName(),event.getGroup().getId(),event.getMessage()));
                    IOHandler.getIoHandler().output("MessageChain: ");
                    event.getMessage().stream().map(Object::toString).forEach(IOHandler.getIoHandler() :: output);
                }
                CommandSender now = new CommandSender(event.getSender());
                AtomicBoolean flag = new AtomicBoolean(false);
                updateMessage(now, event.getMessage().contentToString(), event.getMessage().serializeToMiraiCode(), flag);
                if (!flag.get())
                    CommandLine.exec(now, event.getMessage().contentToString());
            });
            friendMessageEventListener = bot.getEventChannel().subscribeAlways(FriendMessageEvent.class, event -> {
                if (debug) {
                    IOHandler.getIoHandler().output(String.format("%s(%d)",event.getFriend().getNick(),event.getFriend().getId()));
                    IOHandler.getIoHandler().output("RawMessageChain: ");
                    event.getMessage().stream().map(Object::toString).forEach(IOHandler.getIoHandler() :: output);
                }
                CommandSender now = new CommandSender(event.getSender());
                AtomicBoolean flag = new AtomicBoolean(false);
                updateMessage(now, event.getMessage().contentToString(), event.getMessage().serializeToMiraiCode(), flag);
                if (!flag.get())
                    CommandLine.exec(now, event.getMessage().contentToString());
            });
            properties = getConfig().getValues();
            if (properties == null)
                properties = Maps.newHashMap();
            File plugins = new File("plugins");
            if (plugins.exists())
                for (File file : Objects.requireNonNull(plugins.listFiles(file -> file.getName().endsWith(".jar"))))
                    CommandLine.exec("load plugins/" + file.getName());
            Runtime.getRuntime().addShutdownHook(new Thread("SavingData") {
                @Override
                public void run() {
                    if (isRunning) {
                        for (Plugin plugin : LoadCommand.getPlugins())
                            if (!plugin.equals(MainPlugin.this))
                                CommandLine.exec("unload " + plugin.getName());
                        LoadCommand.disablePlugin(MainPlugin.this);
                        friendMessageEventListener.complete();
                        groupMessageEventListener.complete();
                    }
                }
            });
        }

        @Override
        public void disable() {
            for (String key : properties.keySet())
                getConfig().set(key, properties.get(key));
            getConfig().save(getConfigFile());
            for (Plugin plugin : LoadCommand.getPlugins())
                if (!plugin.equals(this))
                    CommandLine.exec("unload " + plugin.getName());
            friendMessageEventListener.complete();
            groupMessageEventListener.complete();
            isRunning = false;
            System.exit(0);
        }

    }

    public static class CommandLine {

        public static void exec(String command) {
            exec(CommandSender.CONSOLE, command);
        }

        public static void exec(CommandSender sender, String command) {
            exec(sender, command, sender.getIOHandler());
        }

        public static void exec(CommandSender sender, String command, IOHandler ioHandler) {
            if (sender != CommandSender.CONSOLE)
                IOHandler.getIoHandler().output(sender + " EXEC: " + command);
            List<String> args = Lists.newArrayList();
            StringBuilder stringBuilder = new StringBuilder();
            boolean stack = false;
            for (char c : command.toCharArray())
                if (c == ' ' && !stack) {
                    args.add(stringBuilder.toString());
                    stringBuilder.delete(0, stringBuilder.length());
                } else {
                    if (c == '"')
                        stack = !stack;
                    else
                        stringBuilder.append(c);
                }
            args.add(stringBuilder.toString());
            String name = args.get(0);
            args.remove(0);
            exec1(sender, name, args.toArray(new String[0]), ioHandler);
        }

        private static void exec1(CommandSender sender, String command, String[] args, IOHandler ioHandler) {
            boolean flag = false;
            for (Command com : Command.getCommands())
                if (com.getAli().contains(command) || com.getName().equals(command)) {
                    flag = true;
                    com.execute(sender, args, ioHandler);
                }
            if (!flag && sender == CommandSender.CONSOLE)
                ioHandler.output("");
        }
    }

}
