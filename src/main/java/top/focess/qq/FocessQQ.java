package top.focess.qq;

import com.google.common.collect.Maps;
import com.google.common.io.Files;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.jetbrains.annotations.UnmodifiableView;
import top.focess.command.CommandResult;
import top.focess.qq.api.bot.Bot;
import top.focess.qq.api.bot.BotLoginException;
import top.focess.qq.api.bot.BotManager;
import top.focess.qq.api.bot.contact.Friend;
import top.focess.qq.api.bot.contact.Group;
import top.focess.qq.api.bot.message.Message;
import top.focess.qq.api.bot.message.TextMessage;
import top.focess.qq.api.command.Command;
import top.focess.qq.api.command.CommandLine;
import top.focess.qq.api.command.CommandSender;
import top.focess.qq.api.command.DataCollection;
import top.focess.qq.api.command.converter.CommandDataConverter;
import top.focess.qq.api.command.converter.PermissionDataConverter;
import top.focess.qq.api.command.converter.PluginDataConverter;
import top.focess.qq.api.command.data.CommandBuffer;
import top.focess.qq.api.command.data.PermissionBuffer;
import top.focess.qq.api.command.data.PluginBuffer;
import top.focess.qq.api.event.EventManager;
import top.focess.qq.api.event.EventSubmitException;
import top.focess.qq.api.event.ListenerHandler;
import top.focess.qq.api.event.chat.ConsoleChatEvent;
import top.focess.qq.api.event.server.ServerStartEvent;
import top.focess.qq.api.event.server.ServerStopEvent;
import top.focess.qq.api.net.*;
import top.focess.qq.api.plugin.Plugin;
import top.focess.qq.api.plugin.PluginLoadException;
import top.focess.qq.api.schedule.Schedulers;
import top.focess.qq.api.util.IOHandler;
import top.focess.qq.api.util.config.LangConfig;
import top.focess.qq.api.util.logger.FocessLogger;
import top.focess.qq.core.bot.BotManagerFactory;
import top.focess.qq.core.commands.*;
import top.focess.qq.core.commands.special.*;
import top.focess.qq.core.listeners.ChatListener;
import top.focess.qq.core.listeners.ConsoleListener;
import top.focess.qq.core.listeners.PluginListener;
import top.focess.qq.core.net.*;
import top.focess.qq.core.permission.Permission;
import top.focess.qq.core.plugin.PluginClassLoader;
import top.focess.qq.core.plugin.PluginCoreClassLoader;
import top.focess.qq.core.util.MethodCaller;
import top.focess.scheduler.FocessScheduler;
import top.focess.scheduler.Scheduler;
import top.focess.scheduler.Task;
import top.focess.util.Pair;
import top.focess.util.option.Option;
import top.focess.util.option.OptionParserClassifier;
import top.focess.util.option.Options;
import top.focess.util.option.type.IntegerOptionType;
import top.focess.util.option.type.LongOptionType;
import top.focess.util.option.type.OptionType;
import top.focess.util.serialize.SimpleFocessReader;
import top.focess.util.version.Version;
import uk.org.lidalia.sysoutslf4j.context.SysOutOverSLF4J;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.Future;
import java.util.zip.GZIPOutputStream;
public class FocessQQ {

    private static final Object STOP_LOCK = new Object();
    private static boolean isStopped;

    /**
     * Version of Focess
     */
    private static final Version VERSION;

    static {
        SimpleFocessReader.setDefaultClassFinder(PluginCoreClassLoader::forName);
        SysOutOverSLF4J.sendSystemOutAndErrToSLF4J();
        Version version;
        final Properties properties = new Properties();
        try {
            properties.load(FocessQQ.class.getResourceAsStream("/default.properties"));
            version = new Version(properties.getProperty("version"));
        } catch (final Exception e) {
            version = new Version("build");
        }
        VERSION = version;
    }

    /**
     * The Focess Logger
     */
    private static final FocessLogger LOGGER = new FocessLogger();
    private static final Scheduler SCHEDULER = new FocessScheduler("FocessQQ");

    private static final Scanner SCANNER = new Scanner(System.in);
    private static final Thread CONSOLE_INPUT_THREAD = new Thread(() -> {
        while (hasNextLine()) {
            String input = SCANNER.nextLine();
            try {
                EventManager.submit(new ConsoleChatEvent(new TextMessage(input)));
            } catch (EventSubmitException e) {
                getLogger().thrLang("exception-submit-console-chat-event", e);
            }
        }
    },"ConsoleInputThread");
    private static final Thread SHUTDOWN_HOOK = new Thread("SavingData") {
        @Override
        public void run() {
            if (running) {
                getLogger().fatalLang("fatal-save-data");
                getLogger().debugLang("save-log");
                saveLogFile();
                saved = true;
                disableMainPlugin();
            }
        }
    };
    /**
     * The Main Plugin Instance
     */
    private static final MainPlugin MAIN_PLUGIN = new MainPlugin();
    /**
     * The Bot Manager
     */
    private static BotManager botManager;
    /**
     * The lang config
     */
    private static final LangConfig LANG_CONFIG = MAIN_PLUGIN.getLangConfig();



    private static boolean hasNextLine() {
        while (true) {
            try {
                if (!(System.in.available() == 0)) break;
            } catch (final IOException e) {
                return false;
            }
            try {
                Thread.sleep(10);
            } catch (final InterruptedException e) {
                return false;
            }
        }
        return SCANNER.hasNextLine();
    }

    /**
     * The administrator QQ number
     */
    @Nullable
    private static Long administratorId;
    /**
     * Indicate MainPlugin is running. True after MainPlugin is loaded.
     */
    private static boolean running;
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
     * The default client receiver
     */
    @Nullable
    private static ClientReceiver clientReceiver;
    /**
     * The default server multi receiver
     */
    @Nullable
    private static ServerMultiReceiver udpServerMultiReceiver;
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
    private static boolean saved;

    private static void disableMainPlugin() {
        PluginClassLoader.disablePlugin(MAIN_PLUGIN);
    }

    private static Options options;

    public static Options getOptions() {
        return options;
    }

    /**
     * Get the Friend Mirai instance by its id
     *
     * @param id the friend id
     * @return the Friend Mirai instance
     * @see FocessQQ#getBot()
     * @see Bot#getFriend(long)
     */
    @Nullable
    public static Friend getFriend(final long id) {
        return getBot().getFriend(id);
    }

    /**
     * Get the Group Mirai instance by its id
     *
     * @param id the group id
     * @return the Group Mirai instance
     * @see FocessQQ#getBot()
     * @see Bot#getGroup(long)
     */
    @Nullable
    public static Group getGroup(final long id) {
        return getBot().getGroup(id);
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
        return botManager;
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
    public static ServerReceiver getUdpServerReceiver() {
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
     * @see Plugin#getPlugins()
     */
    @NonNull
    public static @UnmodifiableView List<Plugin> getPlugins() {
        return PluginClassLoader.getPlugins();
    }

    /**
     * Get the plugin by its name
     *
     * @param name the plugin name
     * @return the plugin
     * @see Plugin#getPlugin(String)
     */
    @Nullable
    public static Plugin getPlugin(final String name) {
        return PluginClassLoader.getPlugin(name);
    }

    /**
     * Get the plugin by its class
     *
     * @param cls the plugin class
     * @return the plugin
     * @see Plugin#getPlugin(Class)
     */
    @Nullable
    public static Plugin getPlugin(final Class<? extends Plugin> cls) {
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
            final String str = IOHandler.getConsoleIoHandler().inputMessage().toString();
            if (str.equals("stop")) {
                exit();
                return;
            }
            username = Long.parseLong(str);
            IOHandler.getConsoleIoHandler().outputLang("input-account-password");
            password = IOHandler.getConsoleIoHandler().inputMessage().toString();
        } catch (final Exception e) {
            requestAccountInformation();
        }
    }

    public static void main(final String[] args) {
        if (isStopped)
            return;
//        System.setSecurityManager(new FocessSecurityManager());
        Thread.currentThread().setUncaughtExceptionHandler((t, e) -> {
            getLogger().thrLang("exception-uncaught-exception", e);
            getLogger().fatalLang("fatal-uncaught-exception");
            exit();
        });
        System.out.println("[FocessQQ][Console] -> This is the context from System.out.");

        try {
            getLogger().debugLang("setup-uncaught-exception-handler");
        } catch (final Exception e) {
            e.printStackTrace();
            exit();
            return;
        }

        CONSOLE_INPUT_THREAD.start();
        getLogger().debugLang("start-console-input-thread");

        options = Options.parse(args,
                new OptionParserClassifier("help"),
                new OptionParserClassifier("user", LongOptionType.LONG_OPTION_TYPE, OptionType.DEFAULT_OPTION_TYPE),
                new OptionParserClassifier("server", IntegerOptionType.INTEGER_OPTION_TYPE),
                new OptionParserClassifier("client", OptionType.DEFAULT_OPTION_TYPE, IntegerOptionType.INTEGER_OPTION_TYPE, OptionType.DEFAULT_OPTION_TYPE, IntegerOptionType.INTEGER_OPTION_TYPE, OptionType.DEFAULT_OPTION_TYPE),
                new OptionParserClassifier("sided"),
                new OptionParserClassifier("client", OptionType.DEFAULT_OPTION_TYPE, IntegerOptionType.INTEGER_OPTION_TYPE, OptionType.DEFAULT_OPTION_TYPE),
                new OptionParserClassifier("udp", IntegerOptionType.INTEGER_OPTION_TYPE),
                new OptionParserClassifier("multi"),
                new OptionParserClassifier("admin", LongOptionType.LONG_OPTION_TYPE),
                new OptionParserClassifier("noDefaultPluginLoad"),
                new OptionParserClassifier("debug"),
                new OptionParserClassifier("botManager", OptionType.DEFAULT_OPTION_TYPE),
                new OptionParserClassifier("allowAll")
        );
        Option option = options.get("help");
        if (option != null) {
            getLogger().info("--help");
            getLogger().info("--user <id> <password>");
            getLogger().info("--admin <id>");
            getLogger().info("--server <port>");
            getLogger().info("--client <localhost> <local-port> <host> <port> <name>");
            getLogger().info("--client <host> <port> <name>");
            getLogger().info("--udp <port>");
            getLogger().info("--sided");
            getLogger().info("--multi");
            getLogger().info("--noDefaultPluginLoad");
            getLogger().info("--debug");
            getLogger().info("--botManager <name>");
            getLogger().info("--allowAll");
            saveLogFile();
            getLogger().debugLang("save-log");
            exit();
            return;
        }
        getLogger().infoLang("start-main", getVersion());
        option = options.get("botManager");
        if (option != null)
            try {
                botManager = Objects.requireNonNull(BotManagerFactory.get(option.get(OptionType.DEFAULT_OPTION_TYPE)));
            } catch (final Exception e) {
                botManager = Objects.requireNonNull(BotManagerFactory.get());
            }
        else botManager = Objects.requireNonNull(BotManagerFactory.get());
        option = options.get("user");
        if (option != null) {
            username = option.get(LongOptionType.LONG_OPTION_TYPE);
            password = option.get(OptionType.DEFAULT_OPTION_TYPE);
            getLogger().debugLang("use-given-account");
        }
        option = options.get("admin");
        if (option != null)
            administratorId = option.get(LongOptionType.LONG_OPTION_TYPE);
        final Option sidedOption = options.get("sided");
        final Option multiOption = options.get("multi");
        option = options.get("server");
        if (option != null) {
            if (sidedOption == null)
                try {
                    final FocessSocket focessSocket = new FocessSocket(option.get(IntegerOptionType.INTEGER_OPTION_TYPE));
                    focessSocket.registerReceiver(serverReceiver = new FocessReceiver(focessSocket));
                    socket = focessSocket;
                    getLogger().infoLang("create-focess-socket-server");
                } catch (final Exception e) {
                    getLogger().thrLang("exception-create-focess-socket-server", e);
                }
            else {
                try {
                    final FocessSidedSocket focessSidedSocket = new FocessSidedSocket(option.get(IntegerOptionType.INTEGER_OPTION_TYPE));
                    focessSidedSocket.registerReceiver(serverReceiver = new FocessSidedReceiver());
                    socket = focessSidedSocket;
                    getLogger().infoLang("create-focess-sided-socket-server");
                } catch (final Exception e) {
                    getLogger().thrLang("exception-create-focess-sided-socket-server", e);
                }
            }
        }
        option = options.get("client");
        if (option != null) {
            if (sidedOption == null)
                try {
                    final FocessSocket focessSocket = new FocessSocket(option.get(IntegerOptionType.INTEGER_OPTION_TYPE));
                    final String localhost = option.get(OptionType.DEFAULT_OPTION_TYPE);
                    final String host = option.get(OptionType.DEFAULT_OPTION_TYPE);
                    final int port = option.get(IntegerOptionType.INTEGER_OPTION_TYPE);
                    final String name = option.get(OptionType.DEFAULT_OPTION_TYPE);
                    focessSocket.registerReceiver(clientReceiver = new FocessClientReceiver(focessSocket, localhost, host, port, name));
                    socket = focessSocket;
                    getLogger().infoLang("create-focess-socket-client");
                } catch (final Exception e) {
                    getLogger().thrLang("exception-create-focess-socket-client", e);
                }
            else {
                try {
                    final String host = option.get(OptionType.DEFAULT_OPTION_TYPE);
                    final int port = option.get(IntegerOptionType.INTEGER_OPTION_TYPE);
                    final String name = option.get(OptionType.DEFAULT_OPTION_TYPE);
                    final FocessSidedClientSocket focessSidedClientSocket = new FocessSidedClientSocket(host, port);
                    focessSidedClientSocket.registerReceiver(clientReceiver = new FocessSidedClientReceiver(focessSidedClientSocket, name));
                    socket = focessSidedClientSocket;
                    getLogger().infoLang("create-focess-sided-socket-client");
                } catch (final Exception e) {
                    getLogger().thrLang("exception-create-focess-sided-socket-client", e);
                }
            }
        }
        option = options.get("udp");
        if (option != null) {
            try {
                final FocessUDPSocket focessUDPSocket = new FocessUDPSocket(option.get(IntegerOptionType.INTEGER_OPTION_TYPE));
                if (multiOption == null)
                    focessUDPSocket.registerReceiver(udpServerReceiver = new FocessUDPReceiver(focessUDPSocket));
                else
                    focessUDPSocket.registerReceiver(udpServerMultiReceiver = new FocessUDPMultiReceiver(focessUDPSocket));
                udpSocket = focessUDPSocket;
                getLogger().infoLang("create-focess-udp-socket-client");
            } catch (final IllegalPortException e) {
                getLogger().thrLang("exception-create-focess-udp-socket-client", e);
            }
        }
        try {
            running = true;
            PluginClassLoader.enablePlugin(MAIN_PLUGIN);
            getLogger().debugLang("load-main-plugin");
        } catch (final Exception e) {
            if (e instanceof PluginLoadException && e.getCause() != null && e.getCause() instanceof BotLoginException) {
                getLogger().fatalLang("fatal-default-bot-login-failed", getUsername());
                getLogger().thrLang("exception-default-bot-login-failed", e.getCause());
            } else getLogger().thrLang("exception-load-main-plugin", e);
            exit();
        }
    }

    /**
     * Exit FocessQQ Framework.
     */
    public static void exit() {
        Permission.checkPermission(Permission.EXIT);
        synchronized (STOP_LOCK) {
            if (isStopped)
                return;
            isStopped = true;
        }
        SCHEDULER.run(() -> {
            try {
                getLogger().fatalLang("fatal-exit-failed");
            } catch (final Exception ignored) {}
            System.exit(0);
        }, Duration.ofSeconds(5), "force-exit");
        Runtime.getRuntime().removeShutdownHook(SHUTDOWN_HOOK);
        // need to check if Exit is called by Initialization
        if (MAIN_PLUGIN != null && MAIN_PLUGIN.isEnabled())
            PluginClassLoader.disablePlugin(MAIN_PLUGIN);
        running = false;
        if (getSocket() != null)
            getSocket().close();
        if (getUdpSocket() != null)
            getUdpSocket().close();
        // make sure scheduler is stopped at the end
        Schedulers.closeAll();
        SCHEDULER.close();
        if (!saved) {
            saveLogFile();
            saved = true;
        }
        CONSOLE_INPUT_THREAD.interrupt();
    }

    private static void saveLogFile() {
        try {
            final File latest = new File("logs", "latest.log");
            if (latest.exists()) {
                final String name = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss").format(new Date());
                final File target = new File("logs", name + ".log");
                Files.copy(latest, target);
                final GZIPOutputStream gzipOutputStream = new GZIPOutputStream(java.nio.file.Files.newOutputStream(new File("logs", name + ".gz").toPath()));
                final FileInputStream inputStream = new FileInputStream(target);
                final byte[] buf = new byte[1024];
                int len;
                while ((len = inputStream.read(buf)) > 0)
                    gzipOutputStream.write(buf, 0, len);
                inputStream.close();
                gzipOutputStream.finish();
                gzipOutputStream.close();
                // ignore if failed
                target.delete();
            }
        } catch (final IOException ignored) {}
    }

    public static LangConfig getLangConfig() {
        return LANG_CONFIG;
    }

    /**
     * The MainPlugin Plugin is a core plugin in Bot. It will initialize all default settings and make the Bot login.
     * Never instance it! It will be instanced when bot bootstraps automatically.
     */
    public static final class MainPlugin extends Plugin {

        private static Map<String, Object> properties;
        private ChatListener chatListener;
        private ConsoleListener consoleListener;

        public MainPlugin() {
            if (MethodCaller.getCallerClass() != FocessQQ.class) {
                getLogger().fatalLang("fatal-main-plugin-already-running");
                exit();
            }
        }

        public static Map<String, Object> getProperties() {
            return properties;
        }

        @Override
        public void enable() {
            getLogger().debugLang("start-enable-main-plugin");
            properties = this.getDefaultConfig().getValues();
            if (properties == null)
                properties = Maps.newHashMap();
            getLogger().debugLang("load-default-properties");
            this.registerListener(this.consoleListener = new ConsoleListener());
            this.registerListener(this.chatListener = new ChatListener());
            this.registerListener(new PluginListener());
            getLogger().debugLang("register-default-listeners");
            this.registerBuffer(PluginDataConverter.PLUGIN_DATA_CONVERTER, PluginBuffer::allocate);
            this.registerBuffer(CommandDataConverter.COMMAND_DATA_CONVERTER, CommandBuffer::allocate);
            this.registerBuffer(PermissionDataConverter.PERMISSION_DATA_CONVERTER, PermissionBuffer::allocate);
            getLogger().debugLang("register-default-buffers");
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
            this.registerCommand(new ExecCommand());
            this.registerCommand(new PauseCommand());
            this.registerCommand(new TestCommand());
            getLogger().debugLang("register-default-commands");
            this.registerSpecialArgumentComplexHandler("previous", new PreviousArgumentHandler());
            this.registerSpecialArgumentComplexHandler("next", new NextArgumentHandler());
            this.registerSpecialArgumentComplexHandler("random_int", new RandomIntegerArgumentHandler());
            this.registerSpecialArgumentComplexHandler("random_double", new RandomDoubleArgumentHandler());
            this.registerSpecialArgumentComplexHandler("self", new SelfIdArgumentHandler());
            this.registerSpecialArgumentComplexHandler("target", new TargetIdArgumentHandler());
            this.registerSpecialArgumentComplexHandler("previous_command", new PreviousCommandArgumentHandler());
            this.registerSpecialArgumentComplexHandler("default", new DefaultBotIdArgumentHandler());
            getLogger().debugLang("register-default-special-argument-handlers");
            // first register listener then request account information because the request process may need the listener, especially ConsoleListener
            if (username == null || password == null) {
                requestAccountInformation();
                if (username == null || password == null)
                    return;
                getLogger().debugLang("request-account-information");
            }
            try {
                bot = getBotManager().loginDirectly(username, password, this);
            } catch (final BotLoginException e) {
                getLogger().thrLang("exception-login-default-bot", e);
                exit();
                return;
            }
            getLogger().debugLang("login-default-bot");
            if (options.get("debug") != null)
                CommandLine.exec("debug");
            final File plugins = new File("plugins");
            if (plugins.exists() && plugins.isDirectory() && options.get("noDefaultPluginLoad") == null) {
                final File[] files = plugins.listFiles(file -> file.getName().endsWith(".jar"));
                if (files != null)
                    for (final File file : files)
                        try {
                            final Future<CommandResult> future = CommandLine.exec("load plugins/" + file.getName());
                            future.get();
                        } catch (final Exception e) {
                            getLogger().thrLang("exception-load-default-plugin", e);
                        }
            }
            PluginClassLoader.loadSoftDependentPlugins();
            getLogger().debugLang("load-default-plugins");
            Runtime.getRuntime().addShutdownHook(SHUTDOWN_HOOK);
            getLogger().debugLang("setup-shutdown-hook");
            try {
                EventManager.submit(new ServerStartEvent());
            } catch (final EventSubmitException e) {
                getLogger().thrLang("exception-submit-server-start-event", e);
            }
        }

        @Override
        public void disable() {
            getLogger().debugLang("start-disable-main-plugin");
            try {
                EventManager.submit(new ServerStopEvent());
            } catch (final EventSubmitException e) {
                getLogger().thrLang("exception-submit-server-stop-event", e);
            }
            // first unregister listener then clear all input requests, because the listener may influence the input requests, especially ConsoleListener and ChatListener
            if (this.consoleListener != null)
                this.consoleListener.unregister();
            if (this.chatListener != null)
                this.chatListener.unregister();
            Pair<IOHandler, Task> consoleElement;
            while ((consoleElement = ConsoleListener.QUESTS.poll()) != null) {
                if (consoleElement.getValue().cancel())
                    consoleElement.getKey().input((Message) null);
            }
            for (final CommandSender sender : ChatListener.QUESTS.keySet())
                ChatListener.QUESTS.compute(sender, (k, v) -> {
                    if (v != null) {
                        Pair<IOHandler, Task> element;
                        while ((element = v.poll()) != null)
                            if (element.getValue().cancel()) {
                                element.getKey().input((Message) null);
                            }
                    }
                    return v;
                });
            for (final Plugin plugin : getPlugins())
                if (!plugin.equals(this))
                    try {
                        final Future<CommandResult> future = CommandLine.exec("unload " + plugin.getName());
                        future.get();
                    } catch (final Exception e) {
                        getLogger().thrLang("exception-unload-default-plugin", e);
                    }
            getLogger().debugLang("unload-all-plugins-except-main-plugin");
            if (Command.unregisterAll())
                getLogger().debugLang("commands-not-empty");
            getLogger().debugLang("unregister-all-commands");
            if (ListenerHandler.unregisterAll())
                getLogger().debugLang("listeners-not-empty");
            getLogger().debugLang("unregister-all-listeners");
            if (DataCollection.unregisterAll())
                getLogger().debugLang("buffers-not-empty");
            getLogger().debugLang("unregister-all-buffers");
            if (CommandLine.unregisterAll())
                getLogger().debugLang("special-argument-handlers-not-empty");
            getLogger().debugLang("unregister-all-special-argument-handlers");
            BotManagerFactory.removeAll();
            getLogger().debugLang("remove-all-bots");
            for (final String key : properties.keySet())
                this.getDefaultConfig().set(key, properties.get(key));
            this.getDefaultConfig().save();
            getLogger().debugLang("save-default-properties");
        }

    }


}
