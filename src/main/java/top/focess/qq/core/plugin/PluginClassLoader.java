package top.focess.qq.core.plugin;

import top.focess.qq.FocessQQ;
import top.focess.qq.api.command.Command;
import top.focess.qq.api.command.CommandType;
import top.focess.qq.api.command.DataCollection;
import top.focess.qq.api.event.EventManager;
import top.focess.qq.api.event.ListenerHandler;
import top.focess.qq.api.event.plugin.PluginLoadEvent;
import top.focess.qq.api.event.plugin.PluginUnloadEvent;
import top.focess.qq.api.plugin.Plugin;
import top.focess.qq.api.plugin.PluginDescription;
import top.focess.qq.api.plugin.PluginType;
import top.focess.qq.api.schedule.Schedulers;
import top.focess.qq.api.util.version.Version;
import top.focess.qq.api.util.yaml.YamlConfiguration;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import top.focess.qq.api.exceptions.*;

import java.io.File;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
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

public class PluginClassLoader extends URLClassLoader {
    public static final Map<Class<? extends Plugin>, Plugin> CLASS_PLUGIN_MAP = Maps.newHashMap();
    public static final Map<String, Plugin> NAME_PLUGIN_MAP = Maps.newHashMap();
    public static final List<Plugin> REGISTERED_PLUGINS = Lists.newCopyOnWriteArrayList();
    private static Field PLUGIN_NAME_FIELD,
            PLUGIN_VERSION_FIELD,
            PLUGIN_AUTHOR_FIELD,
            COMMAND_NAME_FIELD,
            COMMAND_ALIASES_FIELD,
            COMMAND_INITIALIZE_FIELD;
    private static Method PLUGIN_INIT_METHOD;

    static {
        try {
            PLUGIN_NAME_FIELD = Plugin.class.getDeclaredField("name");
            PLUGIN_NAME_FIELD.setAccessible(true);
            PLUGIN_VERSION_FIELD = Plugin.class.getDeclaredField("version");
            PLUGIN_VERSION_FIELD.setAccessible(true);
            PLUGIN_AUTHOR_FIELD = Plugin.class.getDeclaredField("author");
            PLUGIN_AUTHOR_FIELD.setAccessible(true);
            COMMAND_NAME_FIELD = Command.class.getDeclaredField("name");
            COMMAND_NAME_FIELD.setAccessible(true);
            COMMAND_ALIASES_FIELD = Command.class.getDeclaredField("aliases");
            COMMAND_ALIASES_FIELD.setAccessible(true);
            COMMAND_INITIALIZE_FIELD = Command.class.getDeclaredField("initialize");
            COMMAND_INITIALIZE_FIELD.setAccessible(true);
            PLUGIN_INIT_METHOD = Plugin.class.getDeclaredMethod("init");
            PLUGIN_INIT_METHOD.setAccessible(true);
        } catch (Exception e) {
            FocessQQ.getLogger().thrLang("exception-init-classloader", e);
        }
    }

    private static final Object LOCK = new Object();
    private static final Map<String, Set<File>> AFTER_PLUGINS_MAP = Maps.newHashMap();
    private static final Map<Class<? extends Annotation>, AnnotationHandler> HANDLERS = Maps.newHashMap();

    private static final List<ResourceHandler> RESOURCE_HANDLERS = Lists.newArrayList();

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
                    PLUGIN_NAME_FIELD.set(plugin,name);
                    PLUGIN_AUTHOR_FIELD.set(plugin,((PluginType) annotation).author());
                    PLUGIN_VERSION_FIELD.set(plugin,new Version(((PluginType) annotation).version()));
                }
                PLUGIN_INIT_METHOD.invoke(plugin);
                classLoader.plugin = plugin;
                return true;
            } catch (Exception e) {
                throw new PluginLoadException((Class<? extends Plugin>) c, e);
            }
        } else throw new IllegalPluginClassException(c);
    };

    public PluginDescription getPluginDescription() {
        return this.pluginDescription;
    }

    private PluginDescription pluginDescription;

    static {
        RESOURCE_HANDLERS.add((name, inputStream, pluginClassLoader) -> {
            if (name.endsWith(".class"))
                try {
                    pluginClassLoader.loadedClasses.add(pluginClassLoader.loadClass(name.replace("/", ".").substring(0, name.length() - 6), true));
                } catch (ClassNotFoundException e) {
                    FocessQQ.getLogger().thrLang("exception-load-class",e);
                }
        });

        RESOURCE_HANDLERS.add((name, inputStream, pluginClassLoader) -> {
            if (name.equals("plugin.yml"))
                pluginClassLoader.pluginDescription = new PluginDescription(YamlConfiguration.load(inputStream));
        });

        HANDLERS.put(CommandType.class, (c, annotation, classLoader) -> {
            CommandType commandType = (CommandType) annotation;
            if (Command.class.isAssignableFrom(c) && !Modifier.isAbstract(c.getModifiers())) {
                try {
                    Plugin plugin = Plugin.getPlugin(commandType.plugin());
                    if (plugin == null)
                        throw new IllegalCommandClassException(c);
                    Command command = (Command) c.newInstance();
                    if (!commandType.name().isEmpty()){
                        COMMAND_NAME_FIELD.set(command,commandType.name());
                        COMMAND_ALIASES_FIELD.set(command,Lists.newArrayList(commandType.aliases()));
                        if (!COMMAND_INITIALIZE_FIELD.getBoolean(command)) {
                            try {
                                command.init();
                            } catch (Exception e) {
                                throw new CommandLoadException((Class<? extends Command>) c, e);
                            }
                            COMMAND_INITIALIZE_FIELD.set(command,true);
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

    /**
     * Used to enable plugin
     *
     * @param plugin the plugin need to be enabled
     * @throws PluginLoaderException    if the classloader of the plugin is not {@link PluginClassLoader}
     * @throws PluginDuplicateException if the plugin name already exists in the registered plugins
     */
    public static void enablePlugin(Plugin plugin) {
        try {
            FocessQQ.getLogger().debugLang("start-enable-plugin",plugin.getName());
            if (getPlugin(plugin.getClass()) != null || getPlugin(plugin.getName()) != null)
                throw new PluginDuplicateException(plugin.getName());
            // no try-catch because it should be noticed by the Plugin User
            plugin.onEnable();
            REGISTERED_PLUGINS.add(plugin);
            CLASS_PLUGIN_MAP.put(plugin.getClass(), plugin);
            NAME_PLUGIN_MAP.put(plugin.getName(), plugin);
            FocessQQ.getLogger().debugLang("end-enable-plugin",plugin.getName());
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
     * @return the plugin jar file, or null if the plugin is MainPlugin
     */
    public static File disablePlugin(Plugin plugin) {
        FocessQQ.getLogger().debugLang("start-disable-plugin",plugin.getName());
        if (plugin != FocessQQ.getMainPlugin()) {
            ListenerHandler.unregister(plugin);
            FocessQQ.getLogger().debugLang("unregister-listeners");
            DataCollection.unregister(plugin);
            FocessQQ.getLogger().debugLang("unregister-buffers");
            Command.unregister(plugin);
            FocessQQ.getLogger().debugLang("unregister-commands");
            Schedulers.close(plugin);
            FocessQQ.getLogger().debugLang("close-schedulers");
        }
        // try-catch because it should take over the process
        try {
            plugin.onDisable();
        } catch (Exception e) {
            FocessQQ.getLogger().thrLang("exception-plugin-disable", e);
        }
        REGISTERED_PLUGINS.remove(plugin);
        CLASS_PLUGIN_MAP.remove(plugin.getClass());
        NAME_PLUGIN_MAP.remove(plugin.getName());
        File ret = null;
        if (plugin.getClass().getClassLoader() instanceof PluginClassLoader)
            try {
                PluginClassLoader loader = (PluginClassLoader) plugin.getClass().getClassLoader();
                PluginCoreClassLoader.LOADERS.remove(loader);
                if (loader != null) {
                    ret = loader.getFile();
                    loader.close();
                }
            } catch (IOException e) {
                FocessQQ.getLogger().thrLang("exception-remove-plugin-loader", e);
            }
        FocessQQ.getLogger().debugLang("remove-plugin-loader");
        FocessQQ.getLogger().debugLang("end-disable-plugin",plugin.getName());
        PluginUnloadEvent pluginUnloadEvent = new PluginUnloadEvent(plugin);
        try {
            EventManager.submit(pluginUnloadEvent);
        } catch (EventSubmitException e) {
            FocessQQ.getLogger().thrLang("exception-submit-plugin-unload-event",e);
        }
        return ret;
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

    public File getFile() {
        return file;
    }

    private final File file;

    public Plugin getPlugin() {
        return plugin;
    }

    private Plugin plugin;

    public Set<Class<?>> getLoadedClasses() {
        return loadedClasses;
    }

    private final Set<Class<?>> loadedClasses = Sets.newHashSet();

    public PluginClassLoader(@NotNull File file) throws MalformedURLException {
        super(new URL[]{file.toURI().toURL()}, PluginCoreClassLoader.DEFAULT_CLASS_LOADER);
        this.file = file;
        PluginCoreClassLoader.LOADERS.add(this);
    }

    public boolean load() {
        synchronized (LOCK) {
            FocessQQ.getLogger().debugLang("start-load-plugin", file.getName());
            try {
                JarFile jarFile = new JarFile(file);
                Enumeration<JarEntry> entries = jarFile.entries();
                while (entries.hasMoreElements()) {
                    JarEntry jarEntry = entries.nextElement();
                    String name = jarEntry.getName();
                    for (ResourceHandler resourceHandler : RESOURCE_HANDLERS)
                        resourceHandler.handle(name,jarFile.getInputStream(jarEntry),this);
                }
                FocessQQ.getLogger().debugLang("load-plugin-classes", loadedClasses.size());
                if (this.pluginDescription == null) {
                    FocessQQ.getLogger().debugLang("plugin-description-not-found");
                    PluginCoreClassLoader.LOADERS.remove(this);
                    return false;
                }
                Class<?> pluginClass = this.findClass(this.pluginDescription.getMain(),false);
                Annotation annotation = pluginClass.getAnnotation(PluginType.class);
                if (annotation != null) {
                    if (!PLUGIN_TYPE_HANDLER.handle(pluginClass, annotation, this)) {
                        PluginCoreClassLoader.LOADERS.remove(this);
                        return false;
                    }
                } else {
                    PluginCoreClassLoader.LOADERS.remove(this);
                    return false;
                }
                enablePlugin(plugin);
                FocessQQ.getLogger().debugLang("load-plugin-class");

                for (Class<?> c : loadedClasses)
                    analyseClass(c);
                FocessQQ.getLogger().debugLang("load-command-class");

                FocessQQ.getLogger().debugLang("load-depend-plugin");
                for (File file : AFTER_PLUGINS_MAP.getOrDefault(plugin.getName(), Sets.newHashSet())) {
                    PluginClassLoader pluginClassLoader = new PluginClassLoader(file);
                    if (pluginClassLoader.load())
                        FocessQQ.getLogger().infoLang("load-depend-plugin-succeed", pluginClassLoader.getPlugin().getName());
                    else {
                        FocessQQ.getLogger().infoLang("load-depend-plugin-failed", file.getName());
                        pluginClassLoader.close();
                    }
                }
                AFTER_PLUGINS_MAP.remove(plugin.getName());

                PluginLoadEvent pluginLoadEvent = new PluginLoadEvent(plugin);
                try {
                    EventManager.submit(pluginLoadEvent);
                } catch (EventSubmitException e) {
                    FocessQQ.getLogger().thrLang("exception-submit-plugin-load-event",e);
                }
            } catch (Exception e) {
                if (e instanceof IllegalStateException)
                    FocessQQ.getLogger().debugLang("plugin-depend-on-other-plugin");
                if (plugin != null) {
                    FocessQQ.getLogger().thrLang("exception-load-plugin-file", e);
                    ListenerHandler.unregister(plugin);
                    DataCollection.unregister(plugin);
                    Command.unregister(plugin);
                }
                PluginCoreClassLoader.LOADERS.remove(this);
                return false;
            }
            FocessQQ.getLogger().debugLang("end-load-plugin", file.getName());
            return true;
        }
    }

    private void analyseClass(@NotNull Class<?> c) {
        for (Class<? extends Annotation> annotation : HANDLERS.keySet()) {
            Annotation a;
            if ((a = c.getAnnotation(annotation)) != null)
                HANDLERS.get(annotation).handle(c, a, this);
        }
    }

    public Class<?> findClass(String name, boolean resolve) throws ClassNotFoundException {
        Class<?> c = null;
        for (Class<?> loadedClass : this.loadedClasses)
            if (loadedClass.getName().equals(name)) {
                c = loadedClass;
                break;
            }
        if (c == null)
            throw new ClassNotFoundException(name);
        if (resolve)
            resolveClass(c);
        return c;
    }
}