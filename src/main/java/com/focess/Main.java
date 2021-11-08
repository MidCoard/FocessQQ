package com.focess;

import com.focess.api.Plugin;
import com.focess.api.bot.Bot;
import com.focess.api.bot.BotManager;
import com.focess.api.command.Command;
import com.focess.api.command.CommandSender;
import com.focess.api.event.EventManager;
import com.focess.api.event.chat.ConsoleChatEvent;
import com.focess.api.event.server.ServerStartEvent;
import com.focess.api.exceptions.BotLoginException;
import com.focess.api.exceptions.EventSubmitException;
import com.focess.api.exceptions.PluginLoadException;
import com.focess.api.util.CombinedFuture;
import com.focess.api.util.IOHandler;
import com.focess.api.util.logger.FocessLogger;
import com.focess.core.bot.SimpleBotManager;
import com.focess.core.commands.*;
import com.focess.core.listener.ChatListener;
import com.focess.core.listener.ConsoleListener;
import com.focess.api.util.Pair;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.io.Files;
import net.mamoe.mirai.contact.Friend;
import net.mamoe.mirai.contact.Group;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.*;
import java.util.zip.GZIPOutputStream;

public class Main {

    private static final ExecutorService EXECUTOR = Executors.newFixedThreadPool(10);
    private static final ScheduledExecutorService SCHEDULED_EXECUTOR_SERVICE = Executors.newScheduledThreadPool(2);

    /**
     * The Bot Manager
     */
    private static final BotManager BOT_MANAGER = new SimpleBotManager();

    /**
     * The Focess Logger
     */
    private static final FocessLogger LOG = new FocessLogger();

    /**
     * The Author QQ number
     */
    private static final long AUTHOR_ID = 2624646185L;

    private static final Scanner SCANNER = new Scanner(System.in);
    private static MainPlugin MAIN_PLUGIN;

    /**
     * Indicate MainPlugin is running. True after MainPlugin is loaded.
     */
    private static boolean running = false;

    private static final Thread SHUTDOWN_HOOK = new Thread("SavingData") {
        @Override
        public void run() {
            if (running) {
                Main.getLogger().fatal("Main Thread shuts down without saving Data. Try to save data now!");
                Main.getLogger().debug("Save log file.");
                saveLogFile();
                saved = true;
                LoadCommand.disablePlugin(MAIN_PLUGIN);
            }
        }
    };

    private static final Thread CONSOLE_THREAD = new Thread(() -> {
        Main.getLogger().debug("Bot is ready.");
        Main.getLogger().debug("Start listening console input.");
        while (SCANNER.hasNextLine()) {
            String input = SCANNER.nextLine();
            try {
                EventManager.submit(new ConsoleChatEvent(input));
            } catch (EventSubmitException e) {
                Main.getLogger().thr("Submit Console Chat Exception",e);
            }
        }
    });

    /**
     * The Mirai Bot user or we call it QQ number
     */
    private static long username;

    /**
     * The Mirai Bot password
     */
    private static String password;

    /**
     * The default Bot
     */
    private static Bot bot;

    private static boolean saved = false;

    /**
     * Get the Friend Mirai instance by its id
     *
     * @see Main#getBot()
     * @see Bot#getFriend(long)
     * @param id the friend id
     * @return the Friend Mirai instance
     */
    @Nullable
    public static Friend getFriend(long id) {
        return Main.getBot().getFriend(id);
    }

    /**
     * Get the Group Mirai instance by its id
     *
     * @see Main#getBot()
     * @see Bot#getGroup(long)
     * @param id the group id
     * @return the Group Mirai instance
     */
    @Nullable
    public static Group getGroup(long id) {
        return Main.getBot().getGroup(id);
    }

    public static boolean isRunning() {
        return running;
    }

    @NotNull
    public static FocessLogger getLogger() {
        return LOG;
    }

    @NotNull
    public static Bot getBot() {
        return bot;
    }

    public static long getUsername() {
        return username;
    }

    public static long getAuthorId() {
        return AUTHOR_ID;
    }

    public static BotManager getBotManager() {
        return BOT_MANAGER;
    }

    /**
     * Get all the loaded plugins
     *
     * @return all the loaded plugins
     */
    public static List<Plugin> getPlugins() {
        return LoadCommand.getPlugins();
    }

    /**
     *
     * get Author as a Friend
     *
     * @return Author as a Friend
     */
    @NotNull
    public static Friend getAuthor() {
        return getBot().getFriendOrFail(getAuthorId());
    }

    private static void requestAccountInformation() {
        try {
            IOHandler.getConsoleIoHandler().output("Please input your default QQ user id: ");
            String str = SCANNER.nextLine();
            if (str.equals("stop")) {
                //won't happen
                if (LoadCommand.getPlugin(MainPlugin.class) != null)
                    Main.exit();
                else {
                    Main.getLogger().debug("Save log file.");
                    saveLogFile();
                    System.exit(0);
                }
            }
            username = Long.parseLong(str);
            IOHandler.getConsoleIoHandler().output("Please input your default QQ password: ");
            password = SCANNER.nextLine();
        } catch (Exception e) {
            requestAccountInformation();
        }
    }

    public static void main(String[] args) {
        IOHandler.getConsoleIoHandler().output("MiraiQQ Bot Start!");
        Thread.currentThread().setUncaughtExceptionHandler((t, e) -> {
            Main.getLogger().thr("Uncaught Exception",e);
            Main.getLogger().fatal("Main Thread throws an uncaught exception. Force shutdown!");
            Main.exit();
        });
        Main.getLogger().debug("Setup default UncaughtExceptionHandler.");

        SCHEDULED_EXECUTOR_SERVICE.schedule(()->{
            synchronized (ConsoleListener.QUESTS) {
                while (!ConsoleListener.QUESTS.isEmpty() && (System.currentTimeMillis() - ConsoleListener.QUESTS.peek().getValue()) > 60 * 10 * 1000)
                    ConsoleListener.QUESTS.poll().getKey().input(null);
            }
            synchronized (ChatListener.QUESTS) {
                for (CommandSender sender : ChatListener.QUESTS.keySet()) {
                    Queue<Pair<IOHandler, Pair<Boolean, Long>>> queue = ChatListener.QUESTS.get(sender);
                    while (!queue.isEmpty() && (System.currentTimeMillis() - queue.peek().getValue().getValue()) > 60 * 10 * 1000)
                        queue.poll().getKey().input(null);
                }
            }
        },1,TimeUnit.MINUTES);

        CONSOLE_THREAD.start();
        Main.getLogger().debug("Start Console Thread.");
        if (args.length == 2) {
            try {
                username = Long.parseLong(args[0]);
                password = args[1];
                Main.getLogger().debug("Use Username and Password as Given.");
            } catch (Exception ignored) {
                requestAccountInformation();
            }
        } else requestAccountInformation();
        try {
            LoadCommand.enablePlugin(new MainPlugin());
            Main.getLogger().debug("Load MainPlugin.");
        } catch (Exception e) {
            if (e instanceof PluginLoadException && e.getCause() != null && e.getCause() instanceof BotLoginException) {
                Main.getLogger().fatal("Default Bot Login Failed, Server Stop.");
            } else Main.getLogger().thr("Load MainPlugin Exception",e);
            Main.exit();
        }
    }

    /**
     * Relogin default Bot using given username and password
     */
    public static void relogin() {
        bot.relogin();
    }

    /**
     * Exit Bot
     */
    public static void exit() {
        SCHEDULED_EXECUTOR_SERVICE.schedule(() -> {
            Main.getLogger().fatal("Main Thread waits for more than 5 sec before shutdown. Force shutdown!");
            Runtime.getRuntime().removeShutdownHook(SHUTDOWN_HOOK);
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

    /**
     *
     * The MainPlugin Plugin is a core plugin in Bot. It will initialize all default settings and make the Bot login.
     * Never instance it! It will be instanced when bot bootstraps automatically.
     *
     */
    public final static class MainPlugin extends Plugin {

        private static Map<String, Object> properties;

        public MainPlugin() {
            super("MainPlugin");
            if (running) {
                Main.getLogger().fatal("Run more that one MainPlugin. Force shutdown!");
                Main.exit();
            }
            MAIN_PLUGIN = this;
        }

        public static Map<String, Object> getProperties() {
            return properties;
        }

        @Override
        public void enable() {
            Main.getLogger().debug("Enable MainPlugin.");
            running = true;
            this.registerListener(new ConsoleListener());
            this.registerListener(new ChatListener());
            Main.getLogger().debug("Register default listeners.");
            properties = getConfig().getValues();
            if (properties == null)
                properties = Maps.newHashMap();
            Main.getLogger().debug("Load properties.");
            Command.register(this, new LoadCommand());
            Command.register(this, new UnloadCommand());
            Command.register(this, new StopCommand());
            Command.register(this, new ReloginCommand());
            Command.register(this, new FriendCommand());
            Command.register(this, new GroupCommand());
            Main.getLogger().debug("Register default commands.");
            bot = getBotManager().login(username,password);
            Main.getLogger().debug("Login default bot.");
            File plugins = new File("plugins");
            if (plugins.exists())
                for (File file : plugins.listFiles(file -> file.getName().endsWith(".jar")))
                    try {
                        Future<Boolean> future = CommandLine.exec("load plugins/" + file.getName());
                        future.get();
                    } catch (Exception e) {
                        Main.getLogger().thr("Load Target Plugin Exception",e);
                    }
            Main.getLogger().debug("Load plugins in 'plugins' folder.");
            Runtime.getRuntime().addShutdownHook(SHUTDOWN_HOOK);
            Main.getLogger().debug("Setup shutdown hook.");
            try {
                EventManager.submit(new ServerStartEvent());
            } catch (EventSubmitException e) {
                Main.getLogger().thr("Submit Server Start Exception", e);
            }
        }

        @Override
        public void disable() {
            Main.getLogger().debug("Disable MainPlugin.");
            for (Plugin plugin : Main.getPlugins())
                if (!plugin.equals(this))
                    try {
                        LoadCommand.disablePlugin(plugin);
                    } catch (Exception e) {
                        Main.getLogger().thr("Unload Target Plugin Exception",e);
                    }
            Main.getLogger().debug("Unload all loaded plugins without MainPlugin.");
            SimpleBotManager.disableAllBotsAndExit();
            Main.getLogger().debug("Close all logined bots.");
            for (String key : properties.keySet())
                getConfig().set(key, properties.get(key));
            getConfig().save(getConfigFile());
            Main.getLogger().debug("Save properties.");
            if (!saved) {
                Main.getLogger().debug("Save log file.");
                saveLogFile();
                saved = true;
            }
            running = false;
            EXECUTOR.shutdownNow();
            SCHEDULED_EXECUTOR_SERVICE.shutdownNow();
            System.exit(0);
        }

    }


    /**
     * The CommandLine Tool Class can be used to exec command with customize executor, arguments and receiver.
     */
    public static class CommandLine {

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
         * @param sender the executor
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
         * @param sender the executor
         * @param command the command CommandSender executes.
         * @param ioHandler the receiver
         * @return a Future representing pending completion of the command
         */
        @NotNull
        public static Future<Boolean> exec(CommandSender sender, String command, IOHandler ioHandler) {
            if (sender != CommandSender.CONSOLE)
                IOHandler.getConsoleIoHandler().output(sender + " exec: " + command);
            else Main.getLogger().consoleInput(command);
            List<String> args = Lists.newArrayList();
            StringBuilder stringBuilder = new StringBuilder();
            boolean stack = false;
            boolean ignore = false;
            for (char c : command.toCharArray()) {
                if (ignore) {
                    ignore = false;
                    switch (c) {
                        case 'a':stringBuilder.append((char)7);break;
                        case 'b':stringBuilder.append((char)8);break;
                        case 'f':stringBuilder.append((char)12);break;
                        case 'n':stringBuilder.append((char)10);break;
                        case 'r':stringBuilder.append((char)13);break;
                        case 't':stringBuilder.append((char)9);break;
                        case 'v':stringBuilder.append((char)11);break;
                        case '0':stringBuilder.append((char)0);break;
                        default:stringBuilder.append(c);break;
                    }
                } else if (c == '\\')
                    ignore = true;
                else if (c == ' ') {
                    if (!stack) {
                        if (stringBuilder.length() > 0){
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
            return exec0(sender, name, args.toArray(new String[0]), ioHandler);
        }

        private static Future<Boolean> exec0(CommandSender sender, String command, String[] args, IOHandler ioHandler) {
            boolean flag = false;
            CombinedFuture ret = new CombinedFuture();
            for (Command com : Command.getCommands())
                if (com.getAliases().stream().anyMatch(i -> i.equalsIgnoreCase(command)) || com.getName().equalsIgnoreCase(command)) {
                    flag = true;
                    ret.combine(EXECUTOR.submit(() -> com.execute(sender, args, ioHandler)));
                }
            if (!flag && sender == CommandSender.CONSOLE)
                ioHandler.output("Unknown Command");
            return ret;
        }
    }

}
