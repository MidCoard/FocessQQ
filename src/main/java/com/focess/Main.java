package com.focess;

import com.focess.api.bot.Bot;
import com.focess.api.bot.BotManager;
import com.focess.api.command.Command;
import com.focess.api.command.CommandSender;
import com.focess.api.command.DataCollection;
import com.focess.api.command.DataConverter;
import com.focess.api.command.converter.CommandDataConverter;
import com.focess.api.command.converter.PluginDataConverter;
import com.focess.api.command.data.*;
import com.focess.api.command.data.StringBuffer;
import com.focess.api.event.EventManager;
import com.focess.api.event.ListenerHandler;
import com.focess.api.event.chat.ConsoleChatEvent;
import com.focess.api.event.command.CommandPrepostEvent;
import com.focess.api.event.server.ServerStartEvent;
import com.focess.api.exceptions.BotLoginException;
import com.focess.api.exceptions.EventSubmitException;
import com.focess.api.exceptions.IllegalPortException;
import com.focess.api.exceptions.PluginLoadException;
import com.focess.api.net.ClientReceiver;
import com.focess.api.net.ServerMultiReceiver;
import com.focess.api.net.ServerReceiver;
import com.focess.api.net.Socket;
import com.focess.api.plugin.Plugin;
import com.focess.api.util.CombinedFuture;
import com.focess.api.util.IOHandler;
import com.focess.api.util.Pair;
import com.focess.api.util.config.LangConfig;
import com.focess.api.util.logger.FocessLogger;
import com.focess.api.util.version.Version;
import com.focess.core.bot.SimpleBotManager;
import com.focess.core.commands.*;
import com.focess.core.listener.ChatListener;
import com.focess.core.listener.ConsoleListener;
import com.focess.core.listener.PluginListener;
import com.focess.core.net.*;
import com.focess.core.plugin.PluginClassLoader;
import com.focess.core.util.option.Option;
import com.focess.core.util.option.OptionParserClassifier;
import com.focess.core.util.option.Options;
import com.focess.core.util.option.type.IntegerOptionType;
import com.focess.core.util.option.type.LongOptionType;
import com.focess.core.util.option.type.OptionType;
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
import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.*;
import java.util.zip.GZIPOutputStream;

public class Main {

    /**
     * Version of Focess
     */
    private static final Version VERSION = new Version(4,0,0,"1000");

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

    /**
     * The Main Plugin Instance
     */
    private static MainPlugin mainPlugin;

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
    private static final LangConfig LANG_CONFIG = new LangConfig(Main.class.getResourceAsStream("/lang.yml"));

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
                Main.getLogger().fatalLang("fatal-save-data");
                Main.getLogger().debugLang("save-log");
                saveLogFile();
                saved = true;
                PluginClassLoader.disablePlugin(mainPlugin);
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
                Main.getLogger().thrLang("exception-submit-console-chat-event",e);
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

    public static @Nullable Socket getUdpSocket() {
        return udpSocket;
    }

    public static @Nullable ServerReceiver getUdpServerReceiver() {
        return udpServerReceiver;
    }

    @Nullable
    public static ServerMultiReceiver getUdpServerMultiReceiver() {
        return udpServerMultiReceiver;
    }

    public static Version getVersion() {
        return VERSION;
    }

    public static Plugin getMainPlugin() {
        return mainPlugin;
    }

    /**
     * Get all the loaded plugins
     *
     * @return all the loaded plugins
     */
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
            IOHandler.getConsoleIoHandler().outputLang("input-account-username");
            String str = IOHandler.getConsoleIoHandler().input();
            if (str.equals("stop")) {
                Main.getLogger().debugLang("save-log");
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

    public static void main(String[] args) {
        Thread.currentThread().setUncaughtExceptionHandler((t, e) -> {
            Main.getLogger().thrLang("exception-uncaught-exception",e);
            Main.getLogger().fatalLang("fatal-uncaught-exception");
            Main.exit();
        });
        try {
            Main.getLogger().debugLang("setup-uncaught-exception-handler");
        } catch (Exception e) {
            e.printStackTrace();
        }

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

        CONSOLE_INPUT_THREAD.start();
        Main.getLogger().debugLang("start-console-input-thread");

        Options options = Options.parse(args,
                new OptionParserClassifier("help"),
                new OptionParserClassifier("user", LongOptionType.LONG_OPTION_TYPE, OptionType.DEFAULT_OPTION_TYPE),
                new OptionParserClassifier("server", IntegerOptionType.INTEGER_OPTION_TYPE),
                new OptionParserClassifier("client",OptionType.DEFAULT_OPTION_TYPE,IntegerOptionType.INTEGER_OPTION_TYPE,OptionType.DEFAULT_OPTION_TYPE,IntegerOptionType.INTEGER_OPTION_TYPE,OptionType.DEFAULT_OPTION_TYPE),
                new OptionParserClassifier("sided"),
                new OptionParserClassifier("client",OptionType.DEFAULT_OPTION_TYPE,IntegerOptionType.INTEGER_OPTION_TYPE,OptionType.DEFAULT_OPTION_TYPE),
                new OptionParserClassifier("udp",IntegerOptionType.INTEGER_OPTION_TYPE),
                new OptionParserClassifier("multi")
        );
        Option option = options.get("help");
        if (option != null) {
            Main.getLogger().info("--help");
            Main.getLogger().info("--user <id> <password>");
            Main.getLogger().info("--server <port>");
            Main.getLogger().info("--client <localhost> <localport> <host> <port> <name>");
            Main.getLogger().info("--client <host> <port> <name>");
            Main.getLogger().info("--udp <port>");
            Main.getLogger().info("--sided");
            Main.getLogger().info("--multi");
            saveLogFile();
            Main.getLogger().debugLang("save-log");
            System.exit(0);
        }
        Main.getLogger().infoLang("start-main");
        option = options.get("user");
        if (option != null) {
            username = option.get(LongOptionType.LONG_OPTION_TYPE);
            password = option.get(OptionType.DEFAULT_OPTION_TYPE);
            Main.getLogger().debugLang("use-given-account");
        }
        Option sidedOption = options.get("sided");
        Option multiOption = options.get("multi");
        option = options.get("server");
        if (option != null) {
            if (sidedOption == null)
                try {
                    FocessSocket focessSocket = new FocessSocket(option.get(IntegerOptionType.INTEGER_OPTION_TYPE));
                    focessSocket.registerReceiver(serverReceiver = new FocessReceiver(focessSocket));
                    Main.socket = focessSocket;
                    Main.getLogger().infoLang("create-focess-socket-server");
                } catch (Exception e) {
                    Main.getLogger().thrLang("exception-create-focess-socket-server",e);
                }
            else {
                try {
                    FocessSidedSocket focessSidedSocket = new FocessSidedSocket(option.get(IntegerOptionType.INTEGER_OPTION_TYPE));
                    focessSidedSocket.registerReceiver(serverReceiver = new FocessSidedReceiver());
                    Main.socket = focessSidedSocket;
                    Main.getLogger().infoLang("create-focess-sided-socket-server");
                } catch (Exception e) {
                    Main.getLogger().thrLang("exception-create-focess-sided-socket-server",e);
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
                    Main.socket = focessSocket;
                    Main.getLogger().infoLang("create-focess-socket-client");
                } catch (Exception e) {
                    Main.getLogger().thrLang("exception-create-focess-socket-client",e);
                }
            else {
                try {
                    String host = option.get(OptionType.DEFAULT_OPTION_TYPE);
                    int port = option.get(IntegerOptionType.INTEGER_OPTION_TYPE);
                    String name = option.get(OptionType.DEFAULT_OPTION_TYPE);
                    FocessSidedClientSocket focessSidedClientSocket = new FocessSidedClientSocket(host, port);
                    focessSidedClientSocket.registerReceiver(clientReceiver = new FocessSidedClientReceiver(focessSidedClientSocket, name));
                    Main.socket = focessSidedClientSocket;
                    Main.getLogger().infoLang("create-focess-sided-socket-client");
                } catch (Exception e) {
                    Main.getLogger().thrLang("exception-create-focess-sided-socket-client",e);
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
                Main.getLogger().infoLang("create-focess-udp-socket-client");
            } catch (IllegalPortException e) {
                Main.getLogger().thrLang("exception-create-focess-udp-socket-client",e);
            }
        }
        try {
            PluginClassLoader.enablePlugin(new MainPlugin());
            Main.getLogger().debugLang("load-main-plugin");
        } catch (Exception e) {
            if (e instanceof PluginLoadException && e.getCause() != null && e.getCause() instanceof BotLoginException) {
                Main.getLogger().fatalLang("fatal-default-bot-login-failed",getUsername());
            } else Main.getLogger().thrLang("exception-load-main-plugin",e);
            Main.exit();
        }
    }

    /**
     * Exit Bot
     */
    public static void exit() {
        SCHEDULED_EXECUTOR_SERVICE.schedule(() -> {
            Main.getLogger().fatalLang("fatal-exit-failed");
            Runtime.getRuntime().removeShutdownHook(SHUTDOWN_HOOK);
            System.exit(0);
        }, 5, TimeUnit.SECONDS);
        if (mainPlugin != null)
            PluginClassLoader.disablePlugin(mainPlugin);
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
                if (!target.delete())
                    Main.getLogger().fatalLang("fatal-delete-log-file-failed", target.getName());
            }
        } catch (IOException e) {
            Main.getLogger().thrLang("exception-save-log",e);
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
            super("MainPlugin","MidCoard",Version.DEFAULT_VERSION);
            if (running) {
                Main.getLogger().fatalLang("fatal-main-plugin-already-running");
                Main.exit();
            }
            mainPlugin = this;
            try {
                Field field = Plugin.class.getDeclaredField("langConfig");
                field.setAccessible(true);
                field.set(this,LANG_CONFIG);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        public static Map<String, Object> getProperties() {
            return properties;
        }

        @Override
        public void enable() {
            Main.getLogger().debugLang("start-enable-main-plugin");
            running = true;
            this.registerListener(new ConsoleListener());
            this.registerListener(new ChatListener());
            this.registerListener(new PluginListener());
            Main.getLogger().debugLang("register-default-listeners");
            // first register listener then request account information because the request process may need the listener, especially ConsoleListener
            if (username == null || password == null) {
                requestAccountInformation();
                Main.getLogger().debugLang("request-account-information");
            }
            this.registerBuffer(DataConverter.DEFAULT_DATA_CONVERTER, StringBuffer::allocate);
            this.registerBuffer(DataConverter.INTEGER_DATA_CONVERTER, IntBuffer::allocate);
            this.registerBuffer(PluginDataConverter.PLUGIN_DATA_CONVERTER, PluginBuffer::allocate);
            this.registerBuffer(CommandDataConverter.COMMAND_DATA_CONVERTER, CommandBuffer::allocate);
            this.registerBuffer(DataConverter.LONG_DATA_CONVERTER, LongBuffer::allocate);
            this.registerBuffer(DataConverter.DOUBLE_DATA_CONVERTER, DoubleBuffer::allocate);
            this.registerBuffer(DataConverter.BOOLEAN_DATA_CONVERTER, BooleanBuffer::allocate);
            Main.getLogger().debugLang("register-default-data-converters");
            properties = getConfig().getValues();
            if (properties == null)
                properties = Maps.newHashMap();
            Main.getLogger().debugLang("load-default-properties");
            this.registerCommand(new LoadCommand());
            this.registerCommand(new UnloadCommand());
            this.registerCommand(new StopCommand());
            this.registerCommand(new FriendCommand());
            this.registerCommand(new GroupCommand());
            this.registerCommand(new BotCommand());
            this.registerCommand(new ReloadCommand());
            this.registerCommand(new CommandCommand());
            this.registerCommand(new PluginCommand());
            Main.getLogger().debugLang("register-default-commands");
            bot = getBotManager().loginDirectly(username,password);
            Main.getLogger().debugLang("login-default-bot");
            File plugins = new File("plugins");
            if (plugins.exists())
                for (File file : plugins.listFiles(file -> file.getName().endsWith(".jar")))
                    try {
                        Future<Boolean> future = CommandLine.exec("load plugins/" + file.getName());
                        future.get();
                    } catch (Exception e) {
                        Main.getLogger().thrLang("exception-load-default-plugin",e);
                    }
            Main.getLogger().debugLang("load-default-plugins");
            Runtime.getRuntime().addShutdownHook(SHUTDOWN_HOOK);
            Main.getLogger().debugLang("setup-shutdown-hook");
            try {
                EventManager.submit(new ServerStartEvent());
            } catch (EventSubmitException e) {
                Main.getLogger().thrLang("exception-submit-server-start-event", e);
            }
        }

        @Override
        public void disable() {
            Main.getLogger().debugLang("start-disable-main-plugin");
            for (Plugin plugin : Main.getPlugins())
                if (!plugin.equals(this))
                    try {
                        PluginClassLoader.disablePlugin(plugin);
                    } catch (Exception e) {
                        Main.getLogger().thrLang("exception-unload-default-plugin",e);
                    }
            Main.getLogger().debugLang("unload-all-plugins-except-main-plugin");
            if (Command.unregisterAll())
                Main.getLogger().debugLang("commands-not-empty");
            if (ListenerHandler.unregisterAll())
                Main.getLogger().debugLang("listeners-not-empty");
            if (DataCollection.unregisterAll())
                Main.getLogger().debugLang("buffers-not-empty");
            SimpleBotManager.removeAll();
            Main.getLogger().debugLang("remove-all-bots");
            for (String key : properties.keySet())
                getDefaultConfig().set(key, properties.get(key));
            getDefaultConfig().save();
            Main.getLogger().debugLang("save-default-properties");
            if (Main.getSocket() != null)
                Main.getSocket().close();
            if (Main.getUdpSocket() != null)
                Main.getUdpSocket().close();
            Main.getLogger().debugLang("close-all-sockets");
            if (!saved) {
                Main.getLogger().debugLang("save-log");
                saveLogFile();
                saved = true;
            }
            EXECUTOR.shutdownNow();
            SCHEDULED_EXECUTOR_SERVICE.shutdownNow();
            running = false;
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
            if (sender == CommandSender.CONSOLE)
                Main.getLogger().consoleInput(command);
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
            return exec0(sender, name, args.toArray(new String[0]), ioHandler,command);
        }

        private static Future<Boolean> exec0(CommandSender sender, String command, String[] args, IOHandler ioHandler,String rawCommand) {
            boolean flag = false;
            CombinedFuture ret = new CombinedFuture();
            for (Command com : Command.getCommands())
                if (com.getAliases().stream().anyMatch(i -> i.equalsIgnoreCase(command)) || com.getName().equalsIgnoreCase(command)) {
                    CommandPrepostEvent event = new CommandPrepostEvent(sender,com,args,ioHandler);
                    try {
                        EventManager.submit(event);
                    } catch (EventSubmitException e) {
                        Main.getLogger().thrLang("exception-submit-command-prepost-event",e);
                    }
                    if (event.isCancelled())
                        continue;
                    if (sender != CommandSender.CONSOLE)
                        IOHandler.getConsoleIoHandler().outputLang("command-exec",sender.toString(),rawCommand);
                    flag = true;
                    ret.combine(EXECUTOR.submit(() -> com.execute(sender, args, ioHandler)));
                }
            if (!flag && sender == CommandSender.CONSOLE)
                ioHandler.outputLang("unknown-command",command);
            return ret;
        }
    }

}
