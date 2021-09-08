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
    private static boolean running = false;
    private static Listener<GroupMessageEvent> groupMessageEventListener;
    private static Listener<FriendMessageEvent> friendMessageEventListener;
    private static long user;
    private static String password;
    private static BotConfiguration configuration;
    private static volatile boolean ready = false;
    private static boolean saved = false;

    private static final Thread CONSOLE_THREAD = new Thread(() -> {
        Main.getLogger().debug("Wait for Bot is ready.");
        while (!ready);
        Main.getLogger().debug("Bot is ready.");
        Main.getLogger().debug("Start listening console input.");
        while (IOHandler.getConsoleIoHandler().hasInput())
            try {
                CommandLine.exec(IOHandler.getConsoleIoHandler().input());
            } catch (Exception e) {
                Main.getLogger().thr("Exec Command Exception",e);
            }
    });

    public static boolean isRunning() {
        return running;
    }

    public static boolean isReady() {
        return ready;
    }

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

    private static void updateInput(CommandSender sender, String content, String miraiContent, AtomicBoolean flag) {
        quests.compute(sender, (k, v) -> {
            if (v != null && !v.isEmpty()) {
                Pair<IOHandler, Boolean> element = v.poll();
                if (element.getValue())
                    element.getKey().input(content);
                else element.getKey().input(miraiContent);
                flag.set(true);
            }
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
        ready = true;
        groupMessageEventListener = bot.getEventChannel().subscribeAlways(GroupMessageEvent.class, event -> {
            GroupChatEvent e = new GroupChatEvent(event.getSender(), event.getMessage());
            try {
                EventManager.submit(e);
            } catch (EventSubmitException eventSubmitException) {
                Main.getLogger().thr("Submit Group Message Exception",eventSubmitException);
            }
            Main.getLogger().debug(String.format("%s(%d,%s) in %s(%d): %s", event.getSender().getNameCard(), event.getSender().getId(), event.getPermission(), event.getGroup().getName(), event.getGroup().getId(), event.getMessage()));
            Main.getLogger().debug("MessageChain: ");
            event.getMessage().stream().map(Object::toString).forEach(Main.getLogger()::debug);
            CommandSender sender = new CommandSender(event.getSender());
            AtomicBoolean flag = new AtomicBoolean(false);
            updateInput(sender, event.getMessage().contentToString(), event.getMessage().serializeToMiraiCode(), flag);
            if (!flag.get())
                CommandLine.exec(sender, event.getMessage().contentToString());
        });
        friendMessageEventListener = bot.getEventChannel().subscribeAlways(FriendMessageEvent.class, event -> {
            FriendChatEvent e = new FriendChatEvent(event.getFriend(), event.getMessage());
            try {
                EventManager.submit(e);
            } catch (EventSubmitException eventSubmitException) {
                Main.getLogger().thr("Submit Friend Message Exception",eventSubmitException);
            }
            Main.getLogger().debug(String.format("%s(%d)", event.getFriend().getNick(), event.getFriend().getId()));
            Main.getLogger().debug("MessageChain: ");
            event.getMessage().stream().map(Object::toString).forEach(Main.getLogger()::debug);
            CommandSender sender = new CommandSender(event.getSender());
            AtomicBoolean flag = new AtomicBoolean(false);
            updateInput(sender, event.getMessage().contentToString(), event.getMessage().serializeToMiraiCode(), flag);
            if (!flag.get())
                CommandLine.exec(sender, event.getMessage().contentToString());
        });
        Main.getLogger().debug("Register message listeners.");
    }

    public static void relogin() {
        BotReloginEvent event = new BotReloginEvent();
        try {
            EventManager.submit(event);
        } catch (EventSubmitException e) {
            Main.getLogger().thr("Submit Relogin Exception",e);
        }
        if (event.isCancelled())
            return;
        //todo need to be checked
        bot.close();
        login();
        Main.getLogger().debug("Relogin.");
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

    private static void requestAccountInfomation() {
        try {
            IOHandler.getConsoleIoHandler().output("Please input your QQ user id: ");
            user = Long.parseLong(scanner.nextLine());
            IOHandler.getConsoleIoHandler().output("Please input your QQ password: ");
            password = scanner.nextLine();
        } catch (Exception e) {
            requestAccountInfomation();
        }
    }

    public static void main(String[] args) {
        Main.getLogger().info("MiraiQQ Bot Start!");
        Thread.currentThread().setUncaughtExceptionHandler((t, e) -> {
            Main.getLogger().thr("Uncaught Exception",e);
            Main.getLogger().fatal("Main Thread throws an uncaught exception. Force shutdown!");
            Main.exit();
        });
        Main.getLogger().debug("Setup default UncaughtExceptionHandler.");
        scanner = new Scanner(System.in);
        if (args.length == 2) {
            try {
                user = Long.parseLong(args[0]);
                password = args[1];
                Main.getLogger().debug("Use username and password as given.");
            } catch (Exception ignored) {
                requestAccountInfomation();
            }
        } else requestAccountInfomation();
        CONSOLE_THREAD.start();
        Main.getLogger().debug("Start Console Thread.");
        try {
            MAIN_PLUGIN = LoadCommand.enablePlugin(MainPlugin.class);
            Main.getLogger().debug("Load MainPlugin.");
        } catch (Exception e) {
            Main.getLogger().thr("Load MainPlugin Exception",e);
        }
    }

    public static void exit() {
        ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(1);
        scheduledExecutorService.schedule(() -> {
            Main.getLogger().fatal("Main Thread waits for more than 5 sec before shutdown. Force shutdown!");
            System.exit(0);
        }, 5, TimeUnit.SECONDS);
        LoadCommand.disablePlugin(MAIN_PLUGIN);
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
            Main.getLogger().thr("Save Log Exception",e);
        }
    }

    public final static class MainPlugin extends Plugin {

        private static Map<String, Object> properties;

        public MainPlugin() {
            super("MainPlugin");
            if (running)
                Main.exit();
        }

        public static Map<String, Object> getProperties() {
            return properties;
        }

        @Override
        public void enable() {
            Main.getLogger().debug("Enable MainPlugin.");
            running = true;
            Command.register(this, new LoadCommand());
            Command.register(this, new UnloadCommand());
            Command.register(this, new StopCommand());
            Command.register(this, new ReloginCommand());
            Command.register(this, new FriendCommand());
            Main.getLogger().debug("Register default commands.");
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
                        Main.getLogger().thr("CAPTCHA Picture Load Exception",e);
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
            Main.getLogger().debug("Setup default Bot configuration.");
            login();
            Main.getLogger().debug("Login.");
            properties = getConfig().getValues();
            if (properties == null)
                properties = Maps.newHashMap();
            Main.getLogger().debug("Load properties.");
            File plugins = new File("plugins");
            if (plugins.exists())
                for (File file : Objects.requireNonNull(plugins.listFiles(file -> file.getName().endsWith(".jar"))))
                    CommandLine.exec("load plugins/" + file.getName());
            Main.getLogger().debug("Load plugins in 'plugins' folder.");
            Runtime.getRuntime().addShutdownHook(new Thread("SavingData") {
                @Override
                public void run() {
                    if (running) {
                        Main.getLogger().fatal("Main Thread shuts down without saving Data. Try to save data now!");
                        Main.getLogger().debug("Save log file.");
                        saveLogFile();
                        saved = true;
                        LoadCommand.disablePlugin(MainPlugin.this);
                    }
                }
            });
            Main.getLogger().debug("Setup shutdown hook.");
        }

        @Override
        public void disable() {
            Main.getLogger().debug("Disable MainPlugin.");
            for (String key : properties.keySet())
                getConfig().set(key, properties.get(key));
            getConfig().save(getConfigFile());
            Main.getLogger().debug("Save properties.");
            for (Plugin plugin : LoadCommand.getPlugins())
                if (!plugin.equals(this))
                    CommandLine.exec("unload " + plugin.getName());
            Main.getLogger().debug("Unload all loaded plugins without MainPlugin.");
            friendMessageEventListener.complete();
            groupMessageEventListener.complete();
            Main.getLogger().debug("Complete all registered listeners.");
            if (!saved) {
                Main.getLogger().debug("Save log file.");
                saveLogFile();
                saved = true;
            }
            ready = false;
            running = false;
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
