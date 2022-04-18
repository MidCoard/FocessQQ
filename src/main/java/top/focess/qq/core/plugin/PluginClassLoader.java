package top.focess.qq.core.plugin;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import top.focess.command.DataConverter;
import top.focess.command.data.DataBuffer;
import top.focess.qq.FocessQQ;
import top.focess.qq.api.command.*;
import top.focess.qq.api.command.converter.DataConverterType;
import top.focess.qq.api.command.converter.IllegalDataConverterClassException;
import top.focess.qq.api.event.*;
import top.focess.qq.api.event.plugin.PluginLoadEvent;
import top.focess.qq.api.event.plugin.PluginUnloadEvent;
import top.focess.qq.api.plugin.*;
import top.focess.qq.api.schedule.Schedulers;
import top.focess.qq.core.bot.SimpleBotManager;
import top.focess.qq.core.debug.Section;
import top.focess.scheduler.Callback;
import top.focess.scheduler.Scheduler;
import top.focess.scheduler.Task;
import top.focess.util.version.Version;
import top.focess.util.yaml.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.net.URLClassLoader;
import java.time.Duration;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class PluginClassLoader extends URLClassLoader {
    private static final Map<Class<? extends Plugin>, Plugin> CLASS_PLUGIN_MAP = Maps.newConcurrentMap();
    private static final Map<String, Plugin> NAME_PLUGIN_MAP = Maps.newConcurrentMap();
    private static final Object LOCK = new Object();
    private static final Map<String, Set<File>> AFTER_PLUGINS_MAP = Maps.newHashMap();
    private static final Map<Class<? extends Annotation>, AnnotationHandler> HANDLERS = Maps.newHashMap();
    private static final Map<Class<? extends Annotation>, FieldAnnotationHandler> FIELD_ANNOTATION_HANDLERS = Maps.newHashMap();
    private static final List<ResourceHandler> RESOURCE_HANDLERS = Lists.newArrayList();
    private static final Scheduler SCHEDULER = Schedulers.newThreadPoolScheduler(FocessQQ.getMainPlugin(), 2, false, "PluginLoader");
    private static final Scheduler GC_SCHEDULER = Schedulers.newFocessScheduler(FocessQQ.getMainPlugin(), "GC");
    private static Field PLUGIN_NAME_FIELD,
            PLUGIN_VERSION_FIELD,
            PLUGIN_AUTHOR_FIELD,
            COMMAND_COMMAND_FIELD;
    private static Method PLUGIN_INIT_METHOD;
    private static final AnnotationHandler PLUGIN_TYPE_HANDLER = (c, annotation, classLoader) -> {
        PluginType pluginType = (PluginType) annotation;
        if (pluginType.depend().length != 0) {
            boolean flag = false;
            for (String p : pluginType.depend())
                if (Plugin.getPlugin(p) == null) {
                    AFTER_PLUGINS_MAP.compute(p, (key, value) -> {
                        if (value == null)
                            value = Sets.newHashSet();
                        value.add(classLoader.file);
                        return value;
                    });
                    flag = true;
                }
            if (flag)
                throw new IllegalStateException("Plugin depends on other plugins, but not all of them are loaded.");
        }
        if (Plugin.class.isAssignableFrom(c) && !Modifier.isAbstract(c.getModifiers())) {
            try {
                Plugin plugin = (Plugin) c.newInstance();
                if (!((PluginType) annotation).name().isEmpty()) {
                    String name = ((PluginType) annotation).name();
                    PLUGIN_NAME_FIELD.set(plugin, name);
                    PLUGIN_AUTHOR_FIELD.set(plugin, ((PluginType) annotation).author());
                    PLUGIN_VERSION_FIELD.set(plugin, new Version(((PluginType) annotation).version()));
                }
                PLUGIN_INIT_METHOD.invoke(plugin);
                classLoader.plugin = plugin;
                return true;
            } catch (Exception e) {
                throw new PluginLoadException((Class<? extends Plugin>) c, e);
            }
        } else throw new IllegalPluginClassException(c);
    };

    static {
        try {
            PLUGIN_NAME_FIELD = Plugin.class.getDeclaredField("name");
            PLUGIN_NAME_FIELD.setAccessible(true);
            PLUGIN_VERSION_FIELD = Plugin.class.getDeclaredField("version");
            PLUGIN_VERSION_FIELD.setAccessible(true);
            PLUGIN_AUTHOR_FIELD = Plugin.class.getDeclaredField("author");
            PLUGIN_AUTHOR_FIELD.setAccessible(true);
            COMMAND_COMMAND_FIELD = Command.class.getDeclaredField("command");
            COMMAND_COMMAND_FIELD.setAccessible(true);
            PLUGIN_INIT_METHOD = Plugin.class.getDeclaredMethod("init");
            PLUGIN_INIT_METHOD.setAccessible(true);
        } catch (final Exception e) {
            FocessQQ.getLogger().thrLang("exception-init-classloader", e);
        }

        RESOURCE_HANDLERS.add((name, inputStream, pluginClassLoader) -> {
            if (name.endsWith(".class"))
                try {
                    pluginClassLoader.loadedClasses.add(pluginClassLoader.loadClass(name.replace("/", ".").substring(0, name.length() - 6), true));
                } catch (final ClassNotFoundException e) {
                    FocessQQ.getLogger().thrLang("exception-load-class", e);
                }
        });

        RESOURCE_HANDLERS.add((name, inputStream, pluginClassLoader) -> {
            if (name.equals("plugin.yml"))
                pluginClassLoader.pluginDescription = new PluginDescription(YamlConfiguration.load(inputStream));
        });

        HANDLERS.put(CommandType.class, (c, annotation, classLoader) -> {
            final CommandType commandType = (CommandType) annotation;
            if (Command.class.isAssignableFrom(c) && !Modifier.isAbstract(c.getModifiers())) {
                try {
                    final Plugin plugin = classLoader.plugin;
                    final Command command = (Command) c.newInstance();
                    if (!commandType.name().isEmpty()) {
                        top.focess.command.Command command1 = new top.focess.command.Command(commandType.name(),commandType.aliases()) {
                            @Override
                            public void init() {
                            }

                            @Override
                            public @NotNull List<String> usage(top.focess.command.CommandSender commandSender) {
                                return command.usage((CommandSender) commandSender);
                            }
                        };
                        COMMAND_COMMAND_FIELD.set(command, command1);
                        command.init();
                    }
                    plugin.registerCommand(command);
                    return true;
                } catch (final Exception e) {
                    if (e instanceof CommandDuplicateException)
                        throw (CommandDuplicateException) e;
                    else if (e instanceof CommandLoadException)
                        throw (CommandLoadException) e;
                    throw new CommandLoadException((Class<? extends Command>) c, e);
                }
            } else throw new IllegalCommandClassException(c);
        });

        HANDLERS.put(ListenerType.class, (c, annotation, classLoader) -> {
            if (Listener.class.isAssignableFrom(c) && !Modifier.isInterface(c.getModifiers()) && !Modifier.isAbstract(c.getModifiers())) {
                try {
                    final Plugin plugin = classLoader.plugin;
                    final Listener listener = (Listener) c.newInstance();
                    plugin.registerListener(listener);
                    return true;
                } catch (final Exception e) {
                    throw new IllegalListenerClassException((Class<? extends Listener>) c, e);
                }
            } else throw new IllegalListenerClassException(c);
        });

        FIELD_ANNOTATION_HANDLERS.put(DataConverterType.class, (field, annotation, classLoader) -> {
            final DataConverterType dataConverterType = (DataConverterType) annotation;
            if (DataConverter.class.isAssignableFrom(field.getType())) {
                try {
                    final Plugin plugin = classLoader.plugin;
                    final DataConverter dataConverter = (DataConverter) field.get(null);
                    final Constructor<DataBuffer<?>> constructor = (Constructor<DataBuffer<?>>) dataConverterType.buffer().getDeclaredConstructor(int.class);
                    constructor.setAccessible(true);
                    plugin.registerBuffer(dataConverter, size -> {
                        try {
                            return constructor.newInstance(size);
                        } catch (final Exception e) {
                            throw new RuntimeException(e);
                        }
                    });
                } catch (final Exception e) {
                    throw new IllegalDataConverterClassException((Class<? extends DataConverter>) field.getType(), e);
                }
            } else throw new IllegalDataConverterClassException(field.getType());
        });

        FIELD_ANNOTATION_HANDLERS.put(SpecialArgumentType.class, (field, annotation, classLoader) -> {
            final SpecialArgumentType specialArgumentType = (SpecialArgumentType) annotation;
            if (SpecialArgumentComplexHandler.class.isAssignableFrom(field.getType())) {
                try {
                    final String name = specialArgumentType.name();
                    final Plugin plugin = classLoader.plugin;
                    plugin.registerSpecialArgumentComplexHandler(name, (SpecialArgumentComplexHandler) field.get(null));
                } catch (final Exception e) {
                    throw new IllegalSpecialArgumentComplexHandlerClassException((Class<? extends SpecialArgumentComplexHandler>) field.getType(), e);
                }
            } else throw new IllegalSpecialArgumentComplexHandlerClassException(field.getType());
        });
    }

    private final JarFile jarFile;
    private final File file;
    private final Set<Class<?>> loadedClasses = Sets.newHashSet();
    private PluginDescription pluginDescription;
    private Plugin plugin;

    public PluginClassLoader(@NotNull final File file) throws IOException {
        super(new URL[]{file.toURI().toURL()}, PluginCoreClassLoader.DEFAULT_CLASS_LOADER);
        this.file = file;
        this.jarFile = new JarFile(file);
        PluginCoreClassLoader.LOADERS.add(this);
    }

    public static void enablePlugin(@NotNull final Plugin plugin) {
        if (plugin.getClass() != FocessQQ.MainPlugin.class) {
            final Task task = SCHEDULER.run(() -> enablePlugin0(plugin));
            final Section section = Section.startSection("plugin-enable", task, Duration.ofSeconds(30));
            try {
                task.join();
            } catch (final ExecutionException | InterruptedException | CancellationException e) {
                if (e.getCause() instanceof PluginLoadException)
                    throw (PluginLoadException) e.getCause();
                else if (e.getCause() instanceof PluginDuplicateException)
                    throw (PluginDuplicateException) e.getCause();
                else if (e.getCause() instanceof PluginUnloadException)
                    throw (PluginUnloadException) e.getCause();
                else FocessQQ.getLogger().debugLang("section-exception", section.getName(), e.getMessage());
            }
            section.stop();
        } else enablePlugin0(plugin);
    }

    private static void enablePlugin0(final Plugin plugin) {
        try {
            FocessQQ.getLogger().debugLang("start-enable-plugin", plugin.getName());
            // no try-catch because it should be noticed by the Plugin User
            plugin.onEnable();
            CLASS_PLUGIN_MAP.put(plugin.getClass(), plugin);
            NAME_PLUGIN_MAP.put(plugin.getName(), plugin);
            FocessQQ.getLogger().debugLang("end-enable-plugin", plugin.getName());
        } catch (final Exception e) {
            if (e instanceof PluginDuplicateException)
                throw (PluginDuplicateException) e;
            throw new PluginLoadException(plugin.getClass(), e);
        }
    }

    @Nullable
    public static File disablePlugin(final Plugin plugin) {
        final Callback<File> callback = SCHEDULER.submit(() -> disablePlugin0(plugin));
        final Section section = Section.startSection("plugin-disable", (Task) callback, Duration.ofSeconds(5));
        File file = null;
        try {
            file = callback.waitCall();
        } catch (final InterruptedException | ExecutionException | CancellationException e) {
            FocessQQ.getLogger().debugLang("section-exception", section.getName(), e.getMessage());
        }
        section.stop();
        if (!GC_SCHEDULER.isClosed())
            GC_SCHEDULER.run(System::gc, Duration.ofSeconds(1));
        return file;
    }

    @Nullable
    public static File disablePlugin0(@NotNull final Plugin plugin) {
        FocessQQ.getLogger().debugLang("start-disable-plugin", plugin.getName());
        // try-catch because it should take over the process
        try {
            plugin.onDisable();
        } catch (final Exception e) {
            FocessQQ.getLogger().thrLang("exception-plugin-disable", e);
        }
        if (plugin != FocessQQ.getMainPlugin()) {
            ListenerHandler.unregister(plugin);
            FocessQQ.getLogger().debugLang("unregister-listeners");
            DataCollection.unregister(plugin);
            FocessQQ.getLogger().debugLang("unregister-buffers");
            Command.unregister(plugin);
            FocessQQ.getLogger().debugLang("unregister-commands");
            Schedulers.close(plugin);
            FocessQQ.getLogger().debugLang("close-schedulers");
            SimpleBotManager.remove(plugin);
            FocessQQ.getLogger().debugLang("remove-bot");
            CommandLine.unregister(plugin);
            FocessQQ.getLogger().debugLang("unregister-special-argument-handlers");
            if (FocessQQ.getSocket() != null)
                FocessQQ.getSocket().unregister(plugin);
            if (FocessQQ.getUdpSocket() != null)
                FocessQQ.getUdpSocket().unregister(plugin);
        }
        CommandSender.clear(plugin);
        FocessQQ.getLogger().debugLang("clear-command-sender-session", plugin.getName());
        CLASS_PLUGIN_MAP.remove(plugin.getClass());
        NAME_PLUGIN_MAP.remove(plugin.getName());
        File ret = null;
        if (plugin.getClass().getClassLoader() instanceof PluginClassLoader)
            try {
                final PluginClassLoader loader = (PluginClassLoader) plugin.getClass().getClassLoader();
                PluginCoreClassLoader.LOADERS.remove(loader);
                if (loader != null) {
                    ret = loader.getFile();
                    loader.close();
                }
            } catch (final IOException e) {
                FocessQQ.getLogger().thrLang("exception-remove-plugin-loader", e);
            }
        FocessQQ.getLogger().debugLang("remove-plugin-loader");
        FocessQQ.getLogger().debugLang("end-disable-plugin", plugin.getName());
        final PluginUnloadEvent pluginUnloadEvent = new PluginUnloadEvent(plugin);
        try {
            EventManager.submit(pluginUnloadEvent);
        } catch (final EventSubmitException e) {
            FocessQQ.getLogger().thrLang("exception-submit-plugin-unload-event", e);
        }
        return ret;
    }

    @Nullable
    public static <T extends Plugin> T getPlugin(final Class<T> plugin) {
        return (T) CLASS_PLUGIN_MAP.get(plugin);
    }

    @NotNull
    public static List<Plugin> getPlugins() {
        return Lists.newArrayList(NAME_PLUGIN_MAP.values());
    }

    @Nullable
    public static Plugin getPlugin(final String name) {
        return NAME_PLUGIN_MAP.get(name);
    }

    public PluginDescription getPluginDescription() {
        return this.pluginDescription;
    }

    public File getFile() {
        return this.file;
    }

    public Plugin getPlugin() {
        return this.plugin;
    }

    public Set<Class<?>> getLoadedClasses() {
        return this.loadedClasses;
    }

    @Override
    public void close() throws IOException {
        super.close();
        this.loadedClasses.clear();
        this.jarFile.close();
    }

    public boolean load() {
        //make sure only one plugin is loaded at the same time
        synchronized (LOCK) {
            FocessQQ.getLogger().debugLang("start-load-plugin", this.file.getName());
            try {
                final Enumeration<JarEntry> entries = this.jarFile.entries();
                while (entries.hasMoreElements()) {
                    final JarEntry jarEntry = entries.nextElement();
                    final String name = jarEntry.getName();
                    for (final ResourceHandler resourceHandler : RESOURCE_HANDLERS)
                        resourceHandler.handle(name, this.jarFile.getInputStream(jarEntry), this);
                }
                FocessQQ.getLogger().debugLang("load-plugin-classes", this.loadedClasses.size());
                if (this.pluginDescription == null) {
                    FocessQQ.getLogger().debugLang("plugin-description-not-found");
                    PluginCoreClassLoader.LOADERS.remove(this);
                    return false;
                }
                final Class<?> pluginClass = this.findClass(this.pluginDescription.getMain(), false);
                final Annotation annotation = pluginClass.getAnnotation(PluginType.class);
                if (annotation != null) {
                    if (!PLUGIN_TYPE_HANDLER.handle(pluginClass, annotation, this)) {
                        PluginCoreClassLoader.LOADERS.remove(this);
                        return false;
                    }
                } else {
                    PluginCoreClassLoader.LOADERS.remove(this);
                    return false;
                }
                enablePlugin(this.plugin);
                FocessQQ.getLogger().debugLang("load-plugin-class");

                for (final Class<?> c : this.loadedClasses)
                    this.analyseClass(c);
                FocessQQ.getLogger().debugLang("load-class");

                FocessQQ.getLogger().debugLang("load-depend-plugin");
                for (final File file : AFTER_PLUGINS_MAP.getOrDefault(this.plugin.getName(), Sets.newHashSet())) {
                    final PluginClassLoader pluginClassLoader = new PluginClassLoader(file);
                    if (pluginClassLoader.load())
                        FocessQQ.getLogger().infoLang("load-depend-plugin-succeed", pluginClassLoader.getPlugin().getName());
                    else {
                        FocessQQ.getLogger().infoLang("load-depend-plugin-failed", file.getName());
                        pluginClassLoader.close();
                    }
                }
                AFTER_PLUGINS_MAP.remove(this.plugin.getName());

                final PluginLoadEvent pluginLoadEvent = new PluginLoadEvent(this.plugin);
                try {
                    EventManager.submit(pluginLoadEvent);
                } catch (final EventSubmitException e) {
                    FocessQQ.getLogger().thrLang("exception-submit-plugin-load-event", e);
                }
            } catch (final Exception e) {
                if (e instanceof IllegalStateException)
                    FocessQQ.getLogger().debugLang("plugin-depend-on-other-plugin");
                if (this.plugin != null) {
                    if (!(e instanceof PluginUnloadException))
                        FocessQQ.getLogger().thrLang("exception-load-plugin-file", e);
                    else
                        FocessQQ.getLogger().debugLang("plugin-unload-self", this.plugin.getName());
                    ListenerHandler.unregister(this.plugin);
                    DataCollection.unregister(this.plugin);
                    Command.unregister(this.plugin);
                    Schedulers.close(this.plugin);
                    SimpleBotManager.remove(this.plugin);
                    CommandLine.unregister(this.plugin);
                    if (FocessQQ.getSocket() != null)
                        FocessQQ.getSocket().unregister(this.plugin);
                    if (FocessQQ.getUdpSocket() != null)
                        FocessQQ.getUdpSocket().unregister(this.plugin);
                } else if (e instanceof PluginLoadException)
                    FocessQQ.getLogger().thrLang("exception-load-plugin-file", e);
                PluginCoreClassLoader.LOADERS.remove(this);
                return false;
            }
            FocessQQ.getLogger().debugLang("end-load-plugin", this.file.getName());
            return true;
        }
    }

    private void analyseClass(@NotNull final Class<?> c) {
        for (final Class<? extends Annotation> annotation : HANDLERS.keySet()) {
            final Annotation a;
            if ((a = c.getAnnotation(annotation)) != null)
                HANDLERS.get(annotation).handle(c, a, this);
        }
        for (final Field field : c.getDeclaredFields())
            if (Modifier.isStatic(field.getModifiers()))
                for (final Class<? extends Annotation> annotation : FIELD_ANNOTATION_HANDLERS.keySet()) {
                    final Annotation a;
                    if ((a = field.getAnnotation(annotation)) != null) {
                        field.setAccessible(true);
                        FIELD_ANNOTATION_HANDLERS.get(annotation).handle(field, a, this);
                    }
                }
    }

    public Class<?> findClass(final String name, final boolean resolve) throws ClassNotFoundException {
        Class<?> c = null;
        for (final Class<?> loadedClass : this.loadedClasses)
            if (loadedClass.getName().equals(name)) {
                c = loadedClass;
                break;
            }
        if (c == null)
            throw new ClassNotFoundException(name);
        if (resolve)
            this.resolveClass(c);
        return c;
    }

    @Override
    public InputStream getResourceAsStream(final String name) {
        try {
            return this.jarFile.getInputStream(this.jarFile.getEntry(name));
        } catch (final Exception e) {
            return null;
        }
    }
}