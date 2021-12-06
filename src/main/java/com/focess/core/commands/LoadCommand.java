package com.focess.core.commands;

import com.focess.Main;
import com.focess.api.Plugin;
import com.focess.api.annotation.CommandType;
import com.focess.api.annotation.PluginType;
import com.focess.api.command.Command;
import com.focess.api.command.CommandPermission;
import com.focess.api.command.CommandResult;
import com.focess.api.command.CommandSender;
import com.focess.api.event.EventManager;
import com.focess.api.event.ListenerHandler;
import com.focess.api.event.plugin.PluginLoadEvent;
import com.focess.api.exceptions.*;
import com.focess.api.util.IOHandler;
import com.focess.api.util.version.Version;
import com.focess.api.util.yaml.YamlConfiguration;
import com.focess.core.commands.util.AnnotationHandler;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.*;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class LoadCommand extends Command {
    public static final PluginCoreClassLoader DEFAULT_CLASS_LOADER = new PluginCoreClassLoader(LoadCommand.class.getClassLoader());
    private static final List<Plugin> REGISTERED_PLUGINS = Lists.newCopyOnWriteArrayList();
    private static final Map<String, Plugin> NAME_PLUGIN_MAP = Maps.newHashMap();
    private static final Map<Class<? extends Plugin>, Plugin> CLASS_PLUGIN_MAP = Maps.newHashMap();
    private static final Map<Plugin, PluginClassLoader> LOADER_MAP = Maps.newConcurrentMap();
    private static final List<PluginClassLoader> LOADERS = Lists.newCopyOnWriteArrayList();

    public LoadCommand() {
        super("load");
    }

    /**
     * Get Plugin instance by the class instance
     *
     * @param plugin the class instance of the plugin
     * @param <T>    the plugin type
     * @return the plugin instance
     * @see Plugin#getPlugin(Class)
     */
    @Nullable
    public static <T extends Plugin> T getPlugin(Class<T> plugin) {
        return (T) CLASS_PLUGIN_MAP.get(plugin);
    }

    /**
     * Get all plugins registered
     *
     * @return a list of registered plugins
     */
    @NotNull
    public static List<Plugin> getPlugins() {
        return REGISTERED_PLUGINS;
    }

    /**
     * Used to enable plugin
     *
     * @param plugin the plugin need to be enabled
     * @throws PluginLoaderException    if the classloader of the plugin is not {@link PluginClassLoader}
     * @throws PluginDuplicateException if the plugin name already exists in the registered plugins
     */
    public static void enablePlugin(Plugin plugin) {
        try {
            Main.getLogger().debug("Start Enable Plugin " + plugin.getName());
            if (getPlugin(plugin.getClass()) != null || getPlugin(plugin.getName()) != null)
                throw new PluginDuplicateException(plugin.getName());
            plugin.enable();
            Main.getLogger().debug("Enable Plugin.");
            REGISTERED_PLUGINS.add(plugin);
            CLASS_PLUGIN_MAP.put(plugin.getClass(), plugin);
            NAME_PLUGIN_MAP.put(plugin.getName(), plugin);
            Main.getLogger().debug("Add Plugin.");
            Main.getLogger().debug("End Enable Plugin " + plugin.getName());
        } catch (Exception e) {
            if (e instanceof PluginDuplicateException)
                throw (PluginDuplicateException) e;
            throw new PluginLoadException(plugin.getClass(), e);
        }
    }

    /**
     * Used to disable plugin
     *
     * @param plugin the plugin need to be disabled
     */
    public static void disablePlugin(Plugin plugin) {
        Main.getLogger().debug("Start Disable Plugin " + plugin.getName());
        ListenerHandler.unregisterPlugin(plugin);
        Main.getLogger().debug("Unregister Event Listener.");
        Command.unregister(plugin);
        Main.getLogger().debug("Unregister Command.");
        try {
            plugin.disable();
        } catch (Exception e) {
            Main.getLogger().thr("Disable Plugin Exception", e);
        }
        Main.getLogger().debug("Disable Plugin.");
        REGISTERED_PLUGINS.remove(plugin);
        CLASS_PLUGIN_MAP.remove(plugin.getClass());
        NAME_PLUGIN_MAP.remove(plugin.getName());
        Main.getLogger().debug("Remove Plugin.");
        if (plugin.getClass().getClassLoader() instanceof PluginClassLoader)
            try {
                PluginClassLoader loader = LOADER_MAP.remove(plugin);
                if (loader != null)
                    loader.close();
            } catch (IOException e) {
                Main.getLogger().thr("Remove Plugin Loader Exception", e);
            }
        Main.getLogger().debug("Remove Plugin Loader.");
        Main.getLogger().debug("End Disable Plugin " + plugin.getName());
    }

    /**
     * Get Plugin instance by the name
     *
     * @param name the name of the plugin
     * @return the plugin instance
     * @see Plugin#getPlugin(String)
     */
    @Nullable
    public static Plugin getPlugin(String name) {
        return NAME_PLUGIN_MAP.get(name);
    }

    @Override
    public void init() {
        this.setExecutorPermission(CommandSender::isConsole);
        this.addExecutor(1, (sender, data, ioHandler) -> {
            String path = data.get();
            File file = new File(path);
            if (file.exists() && file.getName().endsWith(".jar")) {
                try {
                    PluginClassLoader classLoader = new PluginClassLoader(file);
                    if (classLoader.load())
                        ioHandler.output("Load " + file.getName());
                    else classLoader.close();
                } catch (IOException e) {
                    Main.getLogger().thr("Load Plugin Exception", e);
                    return CommandResult.REFUSE;
                }
                return CommandResult.ALLOW;
            }
            ioHandler.output("File is not existed.");
            return CommandResult.REFUSE;
        });
    }

    @Override
    public void usage(CommandSender sender, IOHandler ioHandler) {
        ioHandler.output("Use: load <path>");
    }

    private static class PluginCoreClassLoader extends ClassLoader {

        public PluginCoreClassLoader(ClassLoader parent) {
            super(parent);
        }

        @Override
        protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
            try {
                return super.loadClass(name, resolve);
            } catch (ClassNotFoundException e) {
                for (PluginClassLoader classLoader : LOADERS)
                    try {
                        return classLoader.findClass(name, resolve);
                    } catch (ClassNotFoundException ignored) {
                    }
            }
            throw new ClassNotFoundException(name);
        }
    }

    public static class PluginClassLoader extends URLClassLoader {
        private static Field PLUGIN_NAME_FIELD,PLUGIN_VERSION_FIELD,PLUGIN_AUTHOR_FIELD,CONFIGURATION_FIELD,CONFIG_FIELD,COMMAND_NAME_FIELD,COMMAND_ALIASES_FIELD,INITIALIZE_FIELD;

        static {
            try {
                PLUGIN_NAME_FIELD = Plugin.class.getDeclaredField("name");
                PLUGIN_NAME_FIELD.setAccessible(true);
                PLUGIN_VERSION_FIELD = Plugin.class.getDeclaredField("version");
                PLUGIN_VERSION_FIELD.setAccessible(true);
                PLUGIN_AUTHOR_FIELD = Plugin.class.getDeclaredField("author");
                PLUGIN_AUTHOR_FIELD.setAccessible(true);
                CONFIGURATION_FIELD = Plugin.class.getDeclaredField("configuration");
                CONFIGURATION_FIELD.setAccessible(true);
                CONFIG_FIELD = Plugin.class.getDeclaredField("config");
                CONFIG_FIELD.setAccessible(true);
                COMMAND_NAME_FIELD = Command.class.getDeclaredField("name");
                COMMAND_NAME_FIELD.setAccessible(true);
                COMMAND_ALIASES_FIELD = Command.class.getDeclaredField("aliases");
                COMMAND_ALIASES_FIELD.setAccessible(true);
                INITIALIZE_FIELD = Command.class.getDeclaredField("initialize");
                INITIALIZE_FIELD.setAccessible(true);
            } catch (NoSuchFieldException e) {
                e.printStackTrace();
            }
        }

        private static final Object LOCK = new Object();
        private static final Map<String, Set<File>> AFTER_PLUGINS_MAP = Maps.newHashMap();
        private static final Map<Class<? extends Annotation>, AnnotationHandler> HANDLERS = Maps.newHashMap();
        private static final AnnotationHandler PLUGIN_TYPE_HANDLER = (c, annotation, classLoader) -> {
            PluginType pluginType = (PluginType) annotation;
            if (pluginType.loadAfter().length != 0) {
                boolean flag = false;
                for (String p : pluginType.loadAfter())
                    if (getPlugin(p) == null) {
                        AFTER_PLUGINS_MAP.compute(p, (key, value) -> {
                            if (value == null)
                                value = Sets.newHashSet();
                            value.add(classLoader.file);
                            return value;
                        });
                        flag = true;
                    }
                if (flag)
                    return false;
            }
            if (Plugin.class.isAssignableFrom(c) && !Modifier.isAbstract(c.getModifiers())) {
                try {
                    Plugin plugin = (Plugin) c.newInstance();
                    if (!((PluginType) annotation).name().isEmpty()) {
                        String name = ((PluginType) annotation).name();
                        PLUGIN_NAME_FIELD.set(plugin,name);
                        PLUGIN_AUTHOR_FIELD.set(plugin,((PluginType) annotation).author());
                        PLUGIN_VERSION_FIELD.set(plugin,new Version(((PluginType) annotation).version()));
                        if (!plugin.getDefaultFolder().exists())
                            if (!plugin.getDefaultFolder().mkdirs())
                                Main.getLogger().debug("Create Default Folder Failed");
                        File config = new File(plugin.getDefaultFolder(), "config.yml");
                        CONFIG_FIELD.set(plugin,config);
                        if (!config.exists()) {
                            try {
                                if (!config.createNewFile())
                                    Main.getLogger().debug("Create Default Config File Failed");
                            } catch (IOException e) {
                                Main.getLogger().thr("Create Config File Exception",e);
                            }
                        }
                        YamlConfiguration configuration = YamlConfiguration.loadFile(plugin.getConfigFile());
                        CONFIGURATION_FIELD.set(plugin,configuration);
                    }
                    classLoader.addPlugin(plugin);
                    return true;
                } catch (Exception e) {
                    if (e instanceof PluginDuplicateException)
                        throw (PluginDuplicateException)e;
                    throw new PluginLoadException((Class<? extends Plugin>) c, e);
                }
            } else throw new IllegalPluginClassException(c);
        };

        static {
            HANDLERS.put(CommandType.class, (c, annotation, classLoader) -> {
                CommandType commandType = (CommandType) annotation;
                if (Command.class.isAssignableFrom(c) && !Modifier.isAbstract(c.getModifiers())) {
                    try {
                        Plugin plugin = getPlugin(commandType.plugin());
                        if (plugin == null)
                            throw new IllegalCommandClassException(c);
                        Command command = (Command) c.newInstance();
                        if (!commandType.name().isEmpty()){
                            COMMAND_NAME_FIELD.set(command,commandType.name());
                            COMMAND_ALIASES_FIELD.set(command,Lists.newArrayList(commandType.aliases()));
                            command.setPermission(CommandPermission.MEMBER);
                            command.setExecutorPermission(i->true);
                            if (!INITIALIZE_FIELD.getBoolean(command)) {
                                try {
                                    command.init();
                                } catch (Exception e) {
                                    throw new CommandLoadException((Class<? extends Command>) c, e);
                                }
                                INITIALIZE_FIELD.set(command,true);
                            }
                        }
                        Command.register(plugin, command);
                        return true;
                    } catch (Exception e) {
                        if (e instanceof CommandDuplicateException)
                            throw (CommandDuplicateException)e;
                        else if (e instanceof CommandLoadException)
                            throw (CommandLoadException)e;
                        throw new CommandLoadException((Class<? extends Command>) c, e);
                    }
                } else throw new IllegalCommandClassException(c);
            });
        }

        private final File file;
        private final Set<Plugin> plugins = Sets.newHashSet();
        private final Set<Class<?>> loadedClasses = Sets.newHashSet();

        public PluginClassLoader(@NotNull File file) throws MalformedURLException {
            super(new URL[]{file.toURI().toURL()}, DEFAULT_CLASS_LOADER);
            this.file = file;
            LOADERS.add(this);
        }

        private void addPlugin(@NotNull Plugin plugin) {
            if (plugins.contains(plugin))
                throw new PluginDuplicateException(plugin.getName());
            plugins.add(plugin);
        }

        public boolean load() {
            synchronized (LOCK) {
                Main.getLogger().debug("Start Load Plugin.");
                try {
                    JarFile jarFile = new JarFile(file);
                    Enumeration<JarEntry> entries = jarFile.entries();
                    while (entries.hasMoreElements()) {
                        JarEntry jarEntry = entries.nextElement();
                        String name = jarEntry.getName();
                        if (!name.endsWith(".class"))
                            continue;
                        this.loadedClasses.add(this.loadClass(name.replace("/", ".").substring(0, name.length() - 6), true));
                    }
                    for (Class<?> c : loadedClasses)
                        if (!analyseClass0(c)) {
                            LOADERS.remove(this);
                            return false;
                        }
                    Main.getLogger().debug("Load And Analyse All Plugin Classes.");
                    for (Plugin plugin : plugins)
                        enablePlugin(plugin);
                    Main.getLogger().debug("Enable All Plugins.");
                    for (Class<?> c : loadedClasses)
                        analyseClass(c);
                    Main.getLogger().debug("Analyse All Non-Plugin Classes.");
                    for (Plugin plugin : plugins) {
                        LOADER_MAP.put(plugin, this);
                        for (File file : AFTER_PLUGINS_MAP.getOrDefault(plugin.getName(), Sets.newHashSet())) {
                            PluginClassLoader pluginClassLoader = new PluginClassLoader(file);
                            if (pluginClassLoader.load())
                                CommandSender.CONSOLE.getIOHandler().output("Load " + file.getName());
                            else {
                                LOADERS.remove(pluginClassLoader);
                                pluginClassLoader.close();
                            }
                        }
                        AFTER_PLUGINS_MAP.remove(plugin.getName());
                        PluginLoadEvent pluginLoadEvent = new PluginLoadEvent(plugin);
                        try {
                            EventManager.submit(pluginLoadEvent);
                        } catch (EventSubmitException e) {
                            Main.getLogger().thr("Submit Plugin Load Exception",e);
                        }
                    }
                    Main.getLogger().debug("Load Plugins Depended On This.");
                } catch (Exception e) {
                    Main.getLogger().thr("Plugin Class Load Exception", e);
                    LOADERS.remove(this);
                    return false;
                }
                Main.getLogger().debug("End Load Plugin.");
                return true;
            }
        }

        private boolean analyseClass0(@NotNull Class<?> c) {
            Annotation annotation;
            if ((annotation = c.getAnnotation(PluginType.class)) != null)
                return PLUGIN_TYPE_HANDLER.handle(c, annotation, this);
            return true;
        }

        private void analyseClass(@NotNull Class<?> c) {
            for (Class<? extends Annotation> annotation : HANDLERS.keySet()) {
                Annotation a;
                if ((a = c.getAnnotation(annotation)) != null)
                    HANDLERS.get(annotation).handle(c, a, this);
            }
        }

        public Class<?> findClass(String name, boolean resolve) throws ClassNotFoundException {
            Class<?> c = this.findLoadedClass(name);
            if (c == null)
                c = this.findClass(name);
            if (c == null)
                //todo know why
                for (Class<?> loadedClass : this.loadedClasses)
                    if (loadedClass.getName().equals(name)) {
                        c = loadedClass;
                        break;
                    }
            if (resolve)
                resolveClass(c);
            return c;
        }
    }

    public static class ObjectInputCoreStream extends ObjectInputStream {

        public ObjectInputCoreStream(InputStream inputStream) throws IOException {
            super(inputStream);
        }

        @Override
        protected Class<?> resolveClass(ObjectStreamClass desc) throws IOException, ClassNotFoundException {
            try {
                return super.resolveClass(desc);
            } catch (ClassNotFoundException e) {
                for (PluginClassLoader classLoader : LOADERS)
                    try {
                        return classLoader.findClass(desc.getName(), true);
                    } catch (ClassNotFoundException ignored) {
                    }
            }
            throw new ClassNotFoundException(desc.getName());
        }
    }
}
