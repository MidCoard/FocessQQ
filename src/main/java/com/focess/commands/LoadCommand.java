package com.focess.commands;

import com.focess.Main;
import com.focess.api.Plugin;
import com.focess.api.annotation.CommandType;
import com.focess.api.annotation.PluginType;
import com.focess.api.command.Command;
import com.focess.api.command.CommandResult;
import com.focess.api.command.CommandSender;
import com.focess.api.event.ListenerHandler;
import com.focess.api.exception.*;
import com.focess.api.util.IOHandler;
import com.focess.commands.util.AnnotationHandler;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import org.checkerframework.checker.nullness.qual.NonNull;

import javax.annotation.Nullable;
import java.io.File;
import java.io.IOException;
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

@SuppressWarnings("unchecked")
public class LoadCommand extends Command {
    public static final PluginCoreClassLoader DEFAULT_CLASS_LOADER = new PluginCoreClassLoader(LoadCommand.class.getClassLoader());
    private static final List<Plugin> REGISTERED_PLUGINS = Lists.newCopyOnWriteArrayList();
    private static final Map<String,Plugin> NAME_PLUGIN_MAP = Maps.newHashMap();
    private static final Map<Class<? extends Plugin>,Plugin> CLASS_PLUGIN_MAP = Maps.newHashMap();
    private static final Map<Plugin, PluginClassLoader> LOADERS = Maps.newHashMap();

    public LoadCommand() {
        super("load", Lists.newArrayList());
    }

    @Nullable
    public static <T extends Plugin> T getPlugin(Class<? extends T> plugin) {
        return (T) CLASS_PLUGIN_MAP.get(plugin);
    }

    @NonNull
    public static List<Plugin> getPlugins() {
        return REGISTERED_PLUGINS;
    }


    public static <T extends Plugin> T enablePlugin(Class<T> cls) {
        try {
            T plugin = cls.newInstance();
            Main.getLogger().debug("Start Enable Plugin " + plugin.getName());
            if (getPlugin(cls) != null)
                throw new PluginDuplicateException(plugin.getName());
            REGISTERED_PLUGINS.add(plugin);
            CLASS_PLUGIN_MAP.put(cls,plugin);
            NAME_PLUGIN_MAP.put(plugin.getName(),plugin);
            Main.getLogger().debug("Add Plugin.");
            plugin.enable();
            Main.getLogger().debug("Enable Plugin.");
            Main.getLogger().debug("End Enable Plugin " + plugin.getName());
            return plugin;
        } catch (Exception e) {
            if (e instanceof PluginDuplicateException)
                throw (PluginDuplicateException) e;
            else throw new PluginLoadException(cls,e);
        }
    }

    public static void enablePlugin(Plugin plugin) {
        if (plugin.getClass().getClassLoader() instanceof PluginClassLoader) {
            try {
                Main.getLogger().debug("Start Enable Plugin " + plugin.getName());
                if (getPlugin(plugin.getClass()) != null)
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
                LOADERS.remove(plugin).close();
            } catch (IOException e) {
                Main.getLogger().thr("Remove Plugin Loader Exception",e);
            }
        Main.getLogger().debug("Remove Plugin Loader.");
        Main.getLogger().debug("End Disable Plugin " + plugin.getName());
    }

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
                for (PluginClassLoader classLoader : LOADERS.values())
                    try {
                        return classLoader.findClass(name, resolve);
                    } catch (ClassNotFoundException ignored) {
                    }
            }
            throw new ClassNotFoundException();
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
            } else if (!pluginType.loadAfter().equals(""))
                CommandSender.CONSOLE.getIOHandler().output("Load " + classLoader.file.getName());
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
                        Command command;
                        if (!Command.register(plugin, command = (Command) c.newInstance()))
                            throw new CommandDuplicateException(command.getName());
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

        public PluginClassLoader(@NonNull File file) throws MalformedURLException {
            super(new URL[]{file.toURI().toURL()}, DEFAULT_CLASS_LOADER);
            this.file = file;
        }

        private boolean addPlugin(@NonNull Plugin plugin) {
            if (plugins.contains(plugin))
                return false;
            if (NAME_PLUGIN_MAP.containsKey(plugin.getName()))
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
                    Class<?> c = this.loadClass(name.replace("/", ".").substring(0, name.length() - 6), true);
                    if (!analyseClass0(c))
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
                    LOADERS.put(plugin, this);
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

        private boolean analyseClass0(@NonNull Class<?> c) {
            Annotation annotation;
            if ((annotation = c.getAnnotation(PluginType.class)) != null)
                return PLUGIN_TYPE_HANDLER.handle(c, annotation, this);
            loadedClasses.add(c);
            return true;
        }

        private void analyseClass(@NonNull Class<?> c) {
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
}
