package com.focess;

import com.focess.api.Plugin;
import com.focess.api.command.Command;
import com.focess.api.command.CommandSender;
import com.focess.api.event.BotReloginEvent;
import com.focess.api.event.EventManager;
import com.focess.api.event.FriendChatEvent;
import com.focess.api.event.GroupChatEvent;
import com.focess.api.exception.EventSubmitException;
import com.focess.api.util.IOHandler;
import com.focess.commands.*;
import com.focess.util.Pair;
import com.focess.util.logger.FocessLogger;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Queues;
import com.google.common.io.Files;
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
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.zip.GZIPOutputStream;

public class Main {

    private static final FocessLogger LOG = new FocessLogger();
    private static final Map<CommandSender, Queue<Pair<IOHandler, Boolean>>> quests = Maps.newHashMap();
    private static final long AUTHOR_USER = 2624646185L;
    private static Scanner scanner;
    private static Bot bot;
    private static MainPlugin MAIN_PLUGIN;
    private static boolean isRunning = false;
    private static Listener<GroupMessageEvent> groupMessageEventListener;
    private static Listener<FriendMessageEvent> friendMessageEventListener;
    private static long user;
    private static String password;
    private static BotConfiguration configuration;
    private static volatile boolean isReady = false;

    private static final Thread CONSOLE_THREAD = new Thread(() -> {
        while (!isReady) ;
        while (IOHandler.getConsoleIoHandler().hasInput())
            try {
                CommandLine.exec(IOHandler.getConsoleIoHandler().input());
            } catch (Exception e) {
                e.printStackTrace();
            }
    });
    private static boolean debug = false;

    public static FocessLogger getLogger() {
        return LOG;
    }

    public static void registerInputListener(IOHandler ioHandler, CommandSender commandSender, boolean flag) {
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

    private static void login() {
        bot = BotFactory.INSTANCE.newBot(user, password, configuration);
        bot.login();
        isReady = true;
        groupMessageEventListener = bot.getEventChannel().subscribeAlways(GroupMessageEvent.class, event -> {
            GroupChatEvent e = new GroupChatEvent(event.getSender(), event.getMessage());
            try {
                EventManager.submit(e);
            } catch (EventSubmitException eventSubmitException) {
                eventSubmitException.printStackTrace();
            }
            if (debug) {
                IOHandler.getConsoleIoHandler().output(String.format("%s(%d,%s) in %s(%d): %s", event.getSender().getNameCard(), event.getSender().getId(), event.getPermission(), event.getGroup().getName(), event.getGroup().getId(), event.getMessage()));
                IOHandler.getConsoleIoHandler().output("MessageChain: ");
                event.getMessage().stream().map(Object::toString).forEach(IOHandler.getConsoleIoHandler()::output);
            }
            CommandSender now = new CommandSender(event.getSender());
            AtomicBoolean flag = new AtomicBoolean(false);
            updateMessage(now, event.getMessage().contentToString(), event.getMessage().serializeToMiraiCode(), flag);
            if (!flag.get())
                CommandLine.exec(now, event.getMessage().contentToString());
        });
        friendMessageEventListener = bot.getEventChannel().subscribeAlways(FriendMessageEvent.class, event -> {
            FriendChatEvent e = new FriendChatEvent(event.getFriend(), event.getMessage());
            try {
                EventManager.submit(e);
            } catch (EventSubmitException eventSubmitException) {
                eventSubmitException.printStackTrace();
            }
            if (debug) {
                IOHandler.getConsoleIoHandler().output(String.format("%s(%d)", event.getFriend().getNick(), event.getFriend().getId()));
                IOHandler.getConsoleIoHandler().output("RawMessageChain: ");
                event.getMessage().stream().map(Object::toString).forEach(IOHandler.getConsoleIoHandler()::output);
            }
            CommandSender now = new CommandSender(event.getSender());
            AtomicBoolean flag = new AtomicBoolean(false);
            updateMessage(now, event.getMessage().contentToString(), event.getMessage().serializeToMiraiCode(), flag);
            if (!flag.get())
                CommandLine.exec(now, event.getMessage().contentToString());
        });
    }

    public static void relogin() {
        BotReloginEvent event = new BotReloginEvent();
        try {
            EventManager.submit(event);
        } catch (EventSubmitException e) {
            e.printStackTrace();
        }
        if (event.isCancelled())
            return;
        //todo need to be checked
        bot.close();
        login();
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
                    element.getKey().input(content);
                else element.getKey().input(valueOf);
                flag.set(true);
            }
            return v;
        });
    }

    private static void requestQQ() {
        try {
            IOHandler.getConsoleIoHandler().output("please input your QQ user number:");
            user = Long.parseLong(scanner.nextLine());
            IOHandler.getConsoleIoHandler().output("please input your QQ password:");
            password = scanner.nextLine();
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
        ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(1);
        scheduledExecutorService.schedule(() -> {
            System.out.println("Force shutdown!");
            System.exit(0);
        }, 5, TimeUnit.SECONDS);
        LoadCommand.disablePlugin(MAIN_PLUGIN);
    }

    public static void setDebug(boolean debug) {
        Main.debug = debug;
    }

    private static void saveLogFile() {
        try {
            File latest = new File("logs", "latest.log");
            if (latest.exists()) {
                String name = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss").format(new Date());
                File target = new File("logs", name + ".log");
                Files.copy(latest, target);
                GZIPOutputStream gzipOutputStream = new GZIPOutputStream(new FileOutputStream(new File("logs", name + ".gz")));
                FileInputStream inputStream = new FileInputStream(target);
                byte[] buf = new byte[1024];
                int len;
                while ((len = inputStream.read(buf)) > 0)
                    gzipOutputStream.write(buf, 0, len);
                inputStream.close();
                gzipOutputStream.finish();
                gzipOutputStream.close();
                target.delete();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
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
            Command.register(this, new DebugCommand());
            Command.register(this, new ReloginCommand());
            Command.register(this, new FriendCommand());
            configuration = BotConfiguration.getDefault();
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
                    IOHandler.getConsoleIoHandler().output(s);
                    scanner.nextLine();
                    return null;
                }

                @Nullable
                @Override
                public Object onSolveUnsafeDeviceLoginVerify(@NotNull Bot bot, @NotNull String s, @NotNull Continuation<? super String> continuation) {
                    IOHandler.getConsoleIoHandler().output(s);
                    scanner.nextLine();
                    return null;
                }
            });
            login();
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
                        saveLogFile();
                        for (String key : properties.keySet())
                            getConfig().set(key, properties.get(key));
                        getConfig().save(getConfigFile());
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
            saveLogFile();
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
                IOHandler.getConsoleIoHandler().output(sender + " exec: " + command);
            else Main.getLogger().consoleInput(command);
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
                ioHandler.output("Unknown Command");
        }
    }

}
