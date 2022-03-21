package top.focess.qq;

import com.google.common.collect.Maps;
import com.google.common.io.Files;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import top.focess.qq.api.bot.*;
import top.focess.qq.api.command.*;
import top.focess.qq.api.command.converter.CommandDataConverter;
import top.focess.qq.api.command.converter.PluginDataConverter;
import top.focess.qq.api.command.data.StringBuffer;
import top.focess.qq.api.command.data.*;
import top.focess.qq.api.event.EventManager;
import top.focess.qq.api.event.EventSubmitException;
import top.focess.qq.api.event.ListenerHandler;
import top.focess.qq.api.event.chat.ConsoleChatEvent;
import top.focess.qq.api.event.server.ServerStartEvent;
import top.focess.qq.api.event.server.ServerStopEvent;
import top.focess.qq.api.net.*;
import top.focess.qq.api.plugin.Plugin;
import top.focess.qq.api.plugin.PluginLoadException;
import top.focess.qq.api.schedule.Scheduler;
import top.focess.qq.api.schedule.Schedulers;
import top.focess.qq.api.util.IOHandler;
import top.focess.qq.api.util.Pair;
import top.focess.qq.api.util.config.LangConfig;
import top.focess.qq.api.util.logger.FocessLogger;
import top.focess.qq.api.util.version.Version;
import top.focess.qq.core.bot.SimpleBotManager;
import top.focess.qq.core.commands.*;
import top.focess.qq.core.commands.special.*;
import top.focess.qq.core.listeners.ChatListener;
import top.focess.qq.core.listeners.ConsoleListener;
import top.focess.qq.core.listeners.PluginListener;
import top.focess.qq.core.net.*;
import top.focess.qq.core.plugin.PluginClassLoader;
import top.focess.qq.core.util.option.Option;
import top.focess.qq.core.util.option.OptionParserClassifier;
import top.focess.qq.core.util.option.Options;
import top.focess.qq.core.util.option.type.IntegerOptionType;
import top.focess.qq.core.util.option.type.LongOptionType;
import top.focess.qq.core.util.option.type.OptionType;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.Future;
import java.util.zip.GZIPOutputStream;

public class FocessQQ {

    /**
     * Version of Focess
     */
    private static final Version VERSION;

    static {
        Version version;
        Properties properties = new Properties();
        try {
            properties.load(FocessQQ.class.getResourceAsStream("/default.properties"));
            version = new Version(properties.getProperty("version"));
        } catch (Exception e) {
            version = new Version("build");
        }
        VERSION = version;
    }


    /**
     * The Focess Logger
     */
    private static final FocessLogger LOGGER = new FocessLogger();

    /**
     * The administrator QQ number
     */
    @Nullable
    private static Long administratorId;

    /**
     * The Main Plugin Instance
     */
    private static final MainPlugin MAIN_PLUGIN = new MainPlugin();

    /**
     * The Bot Manager
     */
    private static final BotManager BOT_MANAGER = new SimpleBotManager();

    private static final Scheduler SCHEDULER = Schedulers.newFocessScheduler(MAIN_PLUGIN,"FocessQQ");

    /**
     * Indicate MainPlugin is running. True after MainPlugin is loaded.
     */
    private static boolean running = false;

    /**
     * The default socket
     */
    @Nullable
    private static Socket socket;

    /**
     * The default udp socket
     */
    @Nullable
    private static Socket udpSocket;

    /**
     * The default udp server receiver
     */
    @Nullable
    private static ServerReceiver udpServerReceiver;

    /**
     * The default server receiver
     */
    @Nullable
    private static ServerReceiver serverReceiver;


    /**
     * The lang config
     */
    private static final LangConfig LANG_CONFIG = MAIN_PLUGIN.getLangConfig();

    /**
     * The default client receiver
     */
    @Nullable
    private static ClientReceiver clientReceiver;

    /**
     * The default server multi receiver
     */
    @Nullable
    private static ServerMultiReceiver udpServerMultiReceiver;

    private static final Thread SHUTDOWN_HOOK = new Thread("SavingData") {
        @Override
        public void run() {
            if (running) {
                FocessQQ.getLogger().fatalLang("fatal-save-data");
                FocessQQ.getLogger().debugLang("save-log");
                saveLogFile();
                saved = true;
                PluginClassLoader.disablePlugin(MAIN_PLUGIN);
            }
        }
    };

    private static final Thread CONSOLE_INPUT_THREAD = new Thread(() -> {
        Scanner scanner = new Scanner(System.in);
        while (scanner.hasNextLine()) {
            String input = scanner.nextLine();
            try {
                EventManager.submit(new ConsoleChatEvent(input));
            } catch (EventSubmitException e) {
                FocessQQ.getLogger().thrLang("exception-submit-console-chat-event",e);
            }
        }
    });

    /**
     * The Mirai Bot user or we call it QQ number
     */
    private static Long username;

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
     * @see FocessQQ#getBot()
     * @see Bot#getFriend(long)
     * @param id the friend id
     * @return the Friend Mirai instance
     */
    @Nullable
    public static Friend getFriend(long id) {
        return FocessQQ.getBot().getFriend(id);
    }

    /**
     * Get the Group Mirai instance by its id
     *
     * @see FocessQQ#getBot()
     * @see Bot#getGroup(long)
     * @param id the group id
     * @return the Group Mirai instance
     */
    @Nullable
    public static Group getGroup(long id) {
        return FocessQQ.getBot().getGroup(id);
    }

    public static boolean isRunning() {
        return running;
    }

    @NonNull
    public static FocessLogger getLogger() {
        return LOGGER;
    }

    @NonNull
    public static Bot getBot() {
        return bot;
    }

    public static long getUsername() {
        return username;
    }

    @Nullable
    public static Long getAdministratorId() {
        return administratorId;
    }

    public static boolean hasAdministratorId() {
        return administratorId != null;
    }

    @NonNull
    public static BotManager getBotManager() {
        return BOT_MANAGER;
    }

    @Nullable
    public static Socket getSocket() {
        return socket;
    }

    @Nullable
    public static ServerReceiver getServerReceiver() {
        return serverReceiver;
    }

    @Nullable
    public static ClientReceiver getClientReceiver() {
        return clientReceiver;
    }

    @Nullable
    public static Socket getUdpSocket() {
        return udpSocket;
    }

    @Nullable
    public static  ServerReceiver getUdpServerReceiver() {
        return udpServerReceiver;
    }

    @Nullable
    public static ServerMultiReceiver getUdpServerMultiReceiver() {
        return udpServerMultiReceiver;
    }

    @NonNull
    public static Version getVersion() {
        return VERSION;
    }

    public static Plugin getMainPlugin() {
        return MAIN_PLUGIN;
    }

    /**
     * Get all the loaded plugins
     *
     * @return all the loaded plugins
     */
    @NonNull
    public static List<Plugin> getPlugins() {
        return PluginClassLoader.getPlugins();
    }

    /**
     * Get the plugin by its name
     *
     * @param name the plugin name
     * @return the plugin
     */
    @Nullable
    public static Plugin getPlugin(String name) {
        return PluginClassLoader.getPlugin(name);
    }

    /**
     * Get the plugin by its class
     *
     * @param cls the plugin class
     * @return the plugin
     */
    @Nullable
    public static Plugin getPlugin(Class<? extends Plugin> cls) {
        return PluginClassLoader.getPlugin(cls);
    }

    /**
     * Get Administrator as a Friend
     *
     * @return the Administrator as a Friend
     */
    @Nullable
    public static Friend getAdministrator() {
        if (administratorId == null)
            return null;
        return getBot().getFriend(administratorId);
    }

    private static void requestAccountInformation() {
        try {
            IOHandler.getConsoleIoHandler().outputLang("input-account-username");
            String str = IOHandler.getConsoleIoHandler().input().trim();
            if (str.equals("stop")) {
                FocessQQ.getLogger().debugLang("save-log");
                saveLogFile();
                System.exit(0);
            }
            username = Long.parseLong(str);
            IOHandler.getConsoleIoHandler().outputLang("input-account-password");
            password = IOHandler.getConsoleIoHandler().input();
        } catch (Exception e) {
            requestAccountInformation();
        }
    }

    private static Options options;

    public static void main(String[] args) {
        Thread.currentThread().setUncaughtExceptionHandler((t, e) -> {
            FocessQQ.getLogger().thrLang("exception-uncaught-exception",e);
            FocessQQ.getLogger().fatalLang("fatal-uncaught-exception");
            FocessQQ.exit();
        });
        try {
            FocessQQ.getLogger().debugLang("setup-uncaught-exception-handler");
        } catch (Exception e) {
            e.printStackTrace();
        }

        SCHEDULER.runTimer(()->{
            Pair<IOHandler, Long> consoleElement = ConsoleListener.QUESTS.poll();
            while (consoleElement != null && System.currentTimeMillis() - consoleElement.getValue() > 60 * 5 * 1000) {
                consoleElement.getKey().input(null);
                consoleElement = ConsoleListener.QUESTS.poll();
            }
            for (CommandSender sender : ChatListener.QUESTS.keySet())
                ChatListener.QUESTS.compute(sender, (k, v) -> {
                    if (v != null) {
                        Pair<IOHandler, Pair<Boolean, Long>> element = v.poll();
                        while (element != null && System.currentTimeMillis() - element.getValue().getValue() > 1000 * 60 * 5) {
                            element.getKey().input(null);
                            element = v.poll();
                        }
                    }
                    return v;
                });
        }, Duration.ZERO,Duration.ofSeconds(10));

        CONSOLE_INPUT_THREAD.start();
        FocessQQ.getLogger().debugLang("start-console-input-thread");

        options = Options.parse(args,
                new OptionParserClassifier("help"),
                new OptionParserClassifier("user", LongOptionType.LONG_OPTION_TYPE, OptionType.DEFAULT_OPTION_TYPE),
                new OptionParserClassifier("server", IntegerOptionType.INTEGER_OPTION_TYPE),
                new OptionParserClassifier("client",OptionType.DEFAULT_OPTION_TYPE,IntegerOptionType.INTEGER_OPTION_TYPE,OptionType.DEFAULT_OPTION_TYPE,IntegerOptionType.INTEGER_OPTION_TYPE,OptionType.DEFAULT_OPTION_TYPE),
                new OptionParserClassifier("sided"),
                new OptionParserClassifier("client",OptionType.DEFAULT_OPTION_TYPE,IntegerOptionType.INTEGER_OPTION_TYPE,OptionType.DEFAULT_OPTION_TYPE),
                new OptionParserClassifier("udp",IntegerOptionType.INTEGER_OPTION_TYPE),
                new OptionParserClassifier("multi"),
                new OptionParserClassifier("admin",LongOptionType.LONG_OPTION_TYPE),
                new OptionParserClassifier("noDefaultPluginLoad")
        );
        Option option = options.get("help");
        if (option != null) {
            FocessQQ.getLogger().info("--help");
            FocessQQ.getLogger().info("--user <id> <password>");
            FocessQQ.getLogger().info("--admin <id>");
            FocessQQ.getLogger().info("--server <port>");
            FocessQQ.getLogger().info("--client <localhost> <localport> <host> <port> <name>");
            FocessQQ.getLogger().info("--client <host> <port> <name>");
            FocessQQ.getLogger().info("--udp <port>");
            FocessQQ.getLogger().info("--sided");
            FocessQQ.getLogger().info("--multi");
            saveLogFile();
            FocessQQ.getLogger().debugLang("save-log");
            return;
        }
        FocessQQ.getLogger().infoLang("start-main", FocessQQ.getVersion());
        option = options.get("user");
        if (option != null) {
            username = option.get(LongOptionType.LONG_OPTION_TYPE);
            password = option.get(OptionType.DEFAULT_OPTION_TYPE);
            FocessQQ.getLogger().debugLang("use-given-account");
        }
        option = options.get("admin");
        if (option != null)
            administratorId = option.get(LongOptionType.LONG_OPTION_TYPE);
        Option sidedOption = options.get("sided");
        Option multiOption = options.get("multi");
        option = options.get("server");
        if (option != null) {
            if (sidedOption == null)
                try {
                    FocessSocket focessSocket = new FocessSocket(option.get(IntegerOptionType.INTEGER_OPTION_TYPE));
                    focessSocket.registerReceiver(serverReceiver = new FocessReceiver(focessSocket));
                    FocessQQ.socket = focessSocket;
                    FocessQQ.getLogger().infoLang("create-focess-socket-server");
                } catch (Exception e) {
                    FocessQQ.getLogger().thrLang("exception-create-focess-socket-server",e);
                }
            else {
                try {
                    FocessSidedSocket focessSidedSocket = new FocessSidedSocket(option.get(IntegerOptionType.INTEGER_OPTION_TYPE));
                    focessSidedSocket.registerReceiver(serverReceiver = new FocessSidedReceiver());
                    FocessQQ.socket = focessSidedSocket;
                    FocessQQ.getLogger().infoLang("create-focess-sided-socket-server");
                } catch (Exception e) {
                    FocessQQ.getLogger().thrLang("exception-create-focess-sided-socket-server",e);
                }
            }
        }
        option = options.get("client");
        if (option != null) {
            if (sidedOption == null)
                try {
                    FocessSocket focessSocket = new FocessSocket(option.get(IntegerOptionType.INTEGER_OPTION_TYPE));
                    String localhost = option.get(OptionType.DEFAULT_OPTION_TYPE);
                    String host = option.get(OptionType.DEFAULT_OPTION_TYPE);
                    int port = option.get(IntegerOptionType.INTEGER_OPTION_TYPE);
                    String name = option.get(OptionType.DEFAULT_OPTION_TYPE);
                    focessSocket.registerReceiver(clientReceiver = new FocessClientReceiver(focessSocket,localhost,host,port,name));
                    FocessQQ.socket = focessSocket;
                    FocessQQ.getLogger().infoLang("create-focess-socket-client");
                } catch (Exception e) {
                    FocessQQ.getLogger().thrLang("exception-create-focess-socket-client",e);
                }
            else {
                try {
                    String host = option.get(OptionType.DEFAULT_OPTION_TYPE);
                    int port = option.get(IntegerOptionType.INTEGER_OPTION_TYPE);
                    String name = option.get(OptionType.DEFAULT_OPTION_TYPE);
                    FocessSidedClientSocket focessSidedClientSocket = new FocessSidedClientSocket(host, port);
                    focessSidedClientSocket.registerReceiver(clientReceiver = new FocessSidedClientReceiver(focessSidedClientSocket, name));
                    FocessQQ.socket = focessSidedClientSocket;
                    FocessQQ.getLogger().infoLang("create-focess-sided-socket-client");
                } catch (Exception e) {
                    FocessQQ.getLogger().thrLang("exception-create-focess-sided-socket-client",e);
                }
            }
        }
        option = options.get("udp");
        if (option != null) {
            try {
                FocessUDPSocket focessUDPSocket = new FocessUDPSocket(option.get(IntegerOptionType.INTEGER_OPTION_TYPE));
                if (multiOption == null)
                    focessUDPSocket.registerReceiver(udpServerReceiver = new FocessUDPReceiver(focessUDPSocket));
                else
                    focessUDPSocket.registerReceiver(udpServerMultiReceiver = new FocessUDPMultiReceiver(focessUDPSocket));
                udpSocket = focessUDPSocket;
                FocessQQ.getLogger().infoLang("create-focess-udp-socket-client");
            } catch (IllegalPortException e) {
                FocessQQ.getLogger().thrLang("exception-create-focess-udp-socket-client",e);
            }
        }
        try {
            PluginClassLoader.enablePlugin(MAIN_PLUGIN);
            FocessQQ.getLogger().debugLang("load-main-plugin");
        } catch (Exception e) {
            if (e instanceof PluginLoadException && e.getCause() != null && e.getCause() instanceof BotLoginException) {
                FocessQQ.getLogger().fatalLang("fatal-default-bot-login-failed",getUsername());
                FocessQQ.getLogger().thrLang("exception-default-bot-login-failed",e.getCause());
            } else FocessQQ.getLogger().thrLang("exception-load-main-plugin",e);
            FocessQQ.exit();
        }
    }

    private static final Object STOP_LOCK = new Object();

    private static Boolean isStopped = false;

    /**
     * Exit Bot
     */
    public static void exit() {
        synchronized (STOP_LOCK) {
            if (isStopped)
                return;
            isStopped = true;
        }
        SCHEDULER.run(() -> {
            FocessQQ.getLogger().fatalLang("fatal-exit-failed");
            Runtime.getRuntime().removeShutdownHook(SHUTDOWN_HOOK);
            System.exit(0);
        }, Duration.ofSeconds(5) );
        if (MAIN_PLUGIN.isEnabled())
            PluginClassLoader.disablePlugin(MAIN_PLUGIN);
    }

    private static void saveLogFile() {
        try {
            File latest = new File("logs", "latest.log");
            if (latest.exists()) {
                String name = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss").format(new Date());
                File target = new File("logs", name + ".log");
                Files.copy(latest, target); // use beta
                GZIPOutputStream gzipOutputStream = new GZIPOutputStream(new FileOutputStream(new File("logs", name + ".gz")));
                FileInputStream inputStream = new FileInputStream(target);
                byte[] buf = new byte[1024];
                int len;
                while ((len = inputStream.read(buf)) > 0)
                    gzipOutputStream.write(buf, 0, len);
                inputStream.close();
                gzipOutputStream.finish();
                gzipOutputStream.close();
                if (!target.delete())
                    FocessQQ.getLogger().fatalLang("fatal-delete-log-file-failed", target.getName());
            }
        } catch (IOException e) {
            FocessQQ.getLogger().thrLang("exception-save-log",e);
        }
    }

    public static LangConfig getLangConfig() {
        return LANG_CONFIG;
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
            super("MainPlugin","MidCoard", FocessQQ.getVersion());
            if (running) {
                FocessQQ.getLogger().fatalLang("fatal-main-plugin-already-running");
                FocessQQ.exit();
            }
        }

        public static Map<String, Object> getProperties() {
            return properties;
        }

        @Override
        public void enable() {
            FocessQQ.getLogger().debugLang("start-enable-main-plugin");
            running = true;
            properties = getDefaultConfig().getValues();
            if (properties == null)
                properties = Maps.newHashMap();
            FocessQQ.getLogger().debugLang("load-default-properties");
            this.registerListener(new ConsoleListener());
            this.registerListener(new ChatListener());
            this.registerListener(new PluginListener());
            FocessQQ.getLogger().debugLang("register-default-listeners");
            this.registerBuffer(DataConverter.DEFAULT_DATA_CONVERTER, StringBuffer::allocate);
            this.registerBuffer(DataConverter.INTEGER_DATA_CONVERTER, IntBuffer::allocate);
            this.registerBuffer(PluginDataConverter.PLUGIN_DATA_CONVERTER, PluginBuffer::allocate);
            this.registerBuffer(CommandDataConverter.COMMAND_DATA_CONVERTER, CommandBuffer::allocate);
            this.registerBuffer(DataConverter.LONG_DATA_CONVERTER, LongBuffer::allocate);
            this.registerBuffer(DataConverter.DOUBLE_DATA_CONVERTER, DoubleBuffer::allocate);
            this.registerBuffer(DataConverter.BOOLEAN_DATA_CONVERTER, BooleanBuffer::allocate);
            FocessQQ.getLogger().debugLang("register-default-buffers");
            this.registerCommand(new LoadCommand());
            this.registerCommand(new UnloadCommand());
            this.registerCommand(new StopCommand());
            this.registerCommand(new FriendCommand());
            this.registerCommand(new GroupCommand());
            this.registerCommand(new BotCommand());
            this.registerCommand(new ReloadCommand());
            this.registerCommand(new CommandCommand());
            this.registerCommand(new PluginCommand());
            this.registerCommand(new DebugCommand());
            FocessQQ.getLogger().debugLang("register-default-commands");
            this.registerSpecialArgumentHandler("previous", new PreviousArgumentHandler());
            this.registerSpecialArgumentHandler("next", new NextArgumentHandler());
            this.registerSpecialArgumentHandler("random_int", new RandomIntegerArgumentHandler());
            this.registerSpecialArgumentHandler("random_double", new RandomDoubleArgumentHandler());
            this.registerSpecialArgumentHandler("self", new SelfIdArgumentHandler());
            this.registerSpecialArgumentHandler("target", new TargetIdArgumentHandler());
            this.registerSpecialArgumentHandler("previous_command",new PreviousCommandArgumentHandler());
            FocessQQ.getLogger().debugLang("register-default-special-argument-handlers");
            // first register listener then request account information because the request process may need the listener, especially ConsoleListener
            if (username == null || password == null) {
                requestAccountInformation();
                FocessQQ.getLogger().debugLang("request-account-information");
            }
            try {
                bot = getBotManager().loginDirectly(username,password,this);
            } catch (BotLoginException e) {
                FocessQQ.getLogger().thrLang("exception-login-default-bot",e);
                FocessQQ.exit();
            }
            FocessQQ.getLogger().debugLang("login-default-bot");
            File plugins = new File("plugins");
            if (plugins.exists() && plugins.isDirectory() && options.get("noDefaultPluginLoad") == null) {
                File[] files = plugins.listFiles(file -> file.getName().endsWith(".jar"));
                if (files != null)
                    for (File file :files)
                        try {
                            Future<CommandResult> future = CommandLine.exec("load plugins/" + file.getName());
                            future.get();
                        } catch (Exception e) {
                            FocessQQ.getLogger().thrLang("exception-load-default-plugin", e);
                        }
            }
            FocessQQ.getLogger().debugLang("load-default-plugins");
            Runtime.getRuntime().addShutdownHook(SHUTDOWN_HOOK);
            FocessQQ.getLogger().debugLang("setup-shutdown-hook");
            try {
                EventManager.submit(new ServerStartEvent());
            } catch (EventSubmitException e) {
                FocessQQ.getLogger().thrLang("exception-submit-server-start-event", e);
            }
        }

        @Override
        public void disable() {
            FocessQQ.getLogger().debugLang("start-disable-main-plugin");
            try {
                EventManager.submit(new ServerStopEvent());
            } catch (EventSubmitException e) {
                FocessQQ.getLogger().thrLang("exception-submit-server-stop-event", e);
            }
            for (Plugin plugin : FocessQQ.getPlugins())
                if (!plugin.equals(this))
                    try {
                        Future<CommandResult> future = CommandLine.exec("unload " + plugin.getName());
                        future.get();
                    } catch (Exception e) {
                        FocessQQ.getLogger().thrLang("exception-unload-default-plugin",e);
                    }
            FocessQQ.getLogger().debugLang("unload-all-plugins-except-main-plugin");
            if (Command.unregisterAll())
                FocessQQ.getLogger().debugLang("commands-not-empty");
            FocessQQ.getLogger().debugLang("unregister-all-commands");
            if (ListenerHandler.unregisterAll())
                FocessQQ.getLogger().debugLang("listeners-not-empty");
            FocessQQ.getLogger().debugLang("unregister-all-listeners");
            if (DataCollection.unregisterAll())
                FocessQQ.getLogger().debugLang("buffers-not-empty");
            FocessQQ.getLogger().debugLang("unregister-all-buffers");
            if (CommandLine.unregisterAll())
                FocessQQ.getLogger().debugLang("special-argument-handlers-not-empty");
            FocessQQ.getLogger().debugLang("unregister-all-special-argument-handlers");
            if (bot != null) {
                SimpleBotManager.removeAll();
                FocessQQ.getLogger().debugLang("remove-all-bots");
            }
            for (String key : properties.keySet())
                getDefaultConfig().set(key, properties.get(key));
            getDefaultConfig().save();
            FocessQQ.getLogger().debugLang("save-default-properties");
            if (FocessQQ.getSocket() != null)
                if (FocessQQ.getSocket().close())
                    FocessQQ.getLogger().debugLang("socket-packet-handler-not-empty");
            if (FocessQQ.getUdpSocket() != null)
                if (FocessQQ.getUdpSocket().close())
                    FocessQQ.getLogger().debugLang("udp-socket-packet-handler-not-empty");
            FocessQQ.getLogger().debugLang("close-all-sockets");
            if (!saved) {
                FocessQQ.getLogger().debugLang("save-log");
                saveLogFile();
            }
            running = false;
            // make sure scheduler is stopped at end of disable
            if (Schedulers.closeAll())
                FocessQQ.getLogger().debugLang("schedulers-not-empty");
            FocessQQ.getLogger().debugLang("unregister-all-schedulers");
            if (!saved) {
                saved = true;
                System.exit(0);
            }
        }

    }


}
