package com.focess.commands;

import com.focess.Main;
import com.focess.api.Plugin;
import com.focess.api.annotation.CommandType;
import com.focess.api.annotation.PluginType;
import com.focess.api.command.Command;
import com.focess.api.command.CommandResult;
import com.focess.api.command.CommandSender;
import com.focess.api.event.ListenerHandler;
import com.focess.api.exceptions.*;
import com.focess.api.util.IOHandler;
import com.focess.commands.util.AnnotationHandler;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.*;
import java.lang.annotation.Annotation;
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
    private static final Map<String,Plugin> NAME_PLUGIN_MAP = Maps.newHashMap();
    private static final Map<Class<? extends Plugin>,Plugin> CLASS_PLUGIN_MAP = Maps.newHashMap();
    private static final Map<Plugin, PluginClassLoader> LOADER_MAP = Maps.newConcurrentMap();
    private static final List<PluginClassLoader> LOADERS = Lists.newCopyOnWriteArrayList();

    public LoadCommand() {
        super("load", Lists.newArrayList());
    }

    /**
     * Get Plugin instance by the class instance
     *
     * @see Plugin#getPlugin(Class)
     * @param plugin the class instance of the plugin
     * @param <T> the plugin type
     * @return the plugin instance
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
     * @throws PluginLoaderException if the classloader of the plugin is not {@link PluginClassLoader}
     * @throws PluginDuplicateException if the plugin name already exists in the registered plugins
     * @throws PluginLoadException if there is any exception thrown in the initializing process
     */
    public static void enablePlugin(Plugin plugin) {
        if (plugin.getClass().getClassLoader() instanceof PluginClassLoader || plugin.getClass() == Main.MainPlugin.class) {
            try {
                Main.getLogger().debug("Start Enable Plugin " + plugin.getName());
                if (getPlugin(plugin.getClass()) != null || getPlugin(plugin.getName()) != null)
                    throw new PluginDuplicateException(plugin.getName());
                REGISTERED_PLUGINS.add(plugin);
                CLASS_PLUGIN_MAP.put(plugin.getClass(),plugin);
                NAME_PLUGIN_MAP.put(plugin.getName(),plugin);
                Main.getLogger().debug("Add Plugin.");
                plugin.enable();
                Main.getLogger().debug("Enable Plugin.");
                Main.getLogger().debug("End Enable Plugin " + plugin.getName());
            } catch (Exception e) {
                if (e instanceof PluginDuplicateException)
                    throw (PluginDuplicateException) e;
                else throw new PluginLoadException(plugin.getClass(),e);
            }
        } else throw new PluginLoaderException(plugin.getName());
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
            Main.getLogger().thr("Disable Plugin Exception",e);
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
                Main.getLogger().thr("Remove Plugin Loader Exception",e);
            }
        Main.getLogger().debug("Remove Plugin Loader.");
        Main.getLogger().debug("End Disable Plugin " + plugin.getName());
    }
    /**
     * Get Plugin instance by the name
     *
     * @see Plugin#getPlugin(String)
     * @param name the name of the plugin
     * @return the plugin instance
     */
    @Nullable
    public static Plugin getPlugin(String name) {
        return NAME_PLUGIN_MAP.get(name);
    }

    @Override
    public void init() {
        this.addExecutor(1, (sender, data, ioHandler) -> {
            if (sender.isConsole()) {
                String path = data.get();
                File file = new File(path);
                if (file.exists() && file.getName().endsWith(".jar")) {
                    try {
                        PluginClassLoader classLoader = new PluginClassLoader(file);
                        if (classLoader.load())
                            ioHandler.output("Load " + file.getName());
                        else classLoader.close();
                    } catch (IOException e) {
                        Main.getLogger().thr("Load Plugin Exception",e);
                        return CommandResult.REFUSE;
                    }
                    return CommandResult.ALLOW;
                }
                ioHandler.output("File is not existed.");
                return CommandResult.REFUSE;
            }
            return CommandResult.REFUSE;
        });
    }

    @Override
    public void usage(CommandSender commandSender, IOHandler ioHandler) {
        if (commandSender.isConsole())
            ioHandler.output("Use: load [plugin-path]");
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

        private static final Map<String, Set<File>> AFTER_PLUGINS_MAP = Maps.newHashMap();

        private static final Set<File> AFTER_PLUGIN_FILES = Sets.newConcurrentHashSet();
        private static final Map<Class<? extends Annotation>, AnnotationHandler> HANDLERS = Maps.newHashMap();
        private static final AnnotationHandler PLUGIN_TYPE_HANDLER = (c, annotation, classLoader) -> {
            PluginType pluginType = (PluginType) annotation;
            if (!pluginType.loadAfter().equals("") && getPlugin(pluginType.loadAfter()) == null) {
                AFTER_PLUGINS_MAP.compute(pluginType.loadAfter(), (key, value) -> {
                    if (value == null)
                        value = Sets.newHashSet();
                    value.add(classLoader.file);
                    return value;
                });
                AFTER_PLUGIN_FILES.add(classLoader.file);
                return false;
            }
            if (Plugin.class.isAssignableFrom(c) && !Modifier.isAbstract(c.getModifiers())) {
                try {
                    Plugin plugin;
                    if (!classLoader.addPlugin(plugin = (Plugin) c.newInstance()))
                        throw new PluginDuplicateException(plugin.getName());
                    return true;
                } catch (Exception e) {
                    throw new PluginLoadException((Class<? extends Plugin>) c,e);
                }
            } else throw new IllegalPluginClassException();
        };

        static {
            HANDLERS.put(CommandType.class, (c, annotation, classLoader) -> {
                CommandType commandType = (CommandType) annotation;
                if (Command.class.isAssignableFrom(c) && !Modifier.isAbstract(c.getModifiers())) {
                    try {
                        Plugin plugin = getPlugin(commandType.plugin());
                        if (plugin == null)
                            throw new IllegalCommandClassException();
                        Command.register(plugin, (Command) c.newInstance());
                    } catch (InstantiationException | IllegalAccessException e) {
                        throw new CommandLoadException((Class<? extends Command>) c);
                    }
                } else throw new IllegalCommandClassException();
                return true;
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

        private boolean addPlugin(@NotNull Plugin plugin) {
            if (plugins.contains(plugin))
                return false;
            plugins.add(plugin);
            return true;
        }

        public boolean load() {
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
                        if (AFTER_PLUGIN_FILES.contains(file)) {
                            PluginClassLoader pluginClassLoader = new PluginClassLoader(file);
                            if (pluginClassLoader.load()) {
                                CommandSender.CONSOLE.getIOHandler().output("Load " + file.getName());
                                AFTER_PLUGIN_FILES.remove(file);
                            }
                        }
                    }
                    AFTER_PLUGINS_MAP.remove(plugin.getName());
                    //todo add multi-depend
                }
                Main.getLogger().debug("Load Plugins Depended On This.");
            } catch (Exception e) {
                Main.getLogger().thr("Plugin Class Load Exception",e);
                return false;
            }
            Main.getLogger().debug("End Load Plugin.");
            return true;
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
                        return classLoader.findClass(desc.getName(),true);
                    } catch (ClassNotFoundException ignored) {
                    }
            }
            throw new ClassNotFoundException(desc.getName());
        }
    }
}
