package top.focess.qq.api.plugin;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import top.focess.qq.FocessQQ;
import top.focess.qq.api.command.Command;
import top.focess.qq.api.command.DataCollection;
import top.focess.qq.api.command.DataConverter;
import top.focess.qq.api.event.Event;
import top.focess.qq.api.event.EventHandler;
import top.focess.qq.api.event.Listener;
import top.focess.qq.api.event.ListenerHandler;
import top.focess.qq.api.exceptions.PluginDuplicateException;
import top.focess.qq.api.exceptions.PluginLoaderException;
import top.focess.qq.api.util.config.DefaultConfig;
import top.focess.qq.api.util.config.LangConfig;
import top.focess.qq.api.util.version.Version;
import top.focess.qq.api.util.yaml.YamlConfiguration;
import top.focess.qq.core.plugin.PluginClassLoader;
import top.focess.qq.core.plugin.PluginCoreClassLoader;
import top.focess.qq.core.util.MethodCaller;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.nio.file.Files;
import java.util.Objects;

/**
 * Represent a Plugin class that can be load, enable and disable. Also, provide plenty of API for the plugin to get better with this framework.
 * You should declare {@link PluginType} to this class.
 */
public abstract class Plugin {

    private static final String path = Plugin.class.getProtectionDomain().getCodeSource().getLocation().getFile();
    /**
     * The plugin author
     */
    private String author;

    /**
     * The plugin version
     */
    private Version version;

    /**
     * The plugin name
     */
    private String name;

    /**
     * The plugin configuration stored in YAML
     */
    @Deprecated
    private YamlConfiguration configuration;

    /**
     * The plugin configuration file
     */
    @Deprecated
    private File config;

    /**
     * The plugin language config
     */
    private LangConfig langConfig;

    /**
     * The plugin default config
     */
    private DefaultConfig defaultConfig;

    /**
     * The plugin description
     */
    private PluginDescription pluginDescription;

    /**
     * Whether the plugin is enabled or not
     */
    private boolean isEnabled = false;

    /**
     * Initialize a Plugin instance by its name.
     * Never instance it! It will be instanced when bot bootstraps automatically.
     *
     * @param name the plugin name
     * @param author the plugin author
     * @param version the plugin version
     * @throws PluginLoaderException if the classloader of the plugin is not {@link PluginClassLoader}
     */
    public Plugin(String name, String author, Version version) {
        this.name = name;
        this.author = author;
        this.version = version;
        this.init();
    }

    /**
     * Provide a constructor to help {@link PluginType} design.
     * Never instance it! It will be instanced when bot bootstraps automatically.
     */
    protected Plugin() {}

    private void init() {
        if (!(this.getClass().getClassLoader() instanceof PluginClassLoader) && this.getClass() != FocessQQ.MainPlugin.class)
            throw new PluginLoaderException(name);
        if (this.getClass() != FocessQQ.MainPlugin.class) {
            if (Plugin.getPlugin(this.getClass()) != null)
                throw new PluginDuplicateException(name,"Cannot new a plugin at runtime");
            this.pluginDescription = new PluginDescription(YamlConfiguration.load(loadResource("plugin.yml")));
        }
        else this.pluginDescription = new PluginDescription();
        if (!this.getClass().getName().equals(this.pluginDescription.getMain()))
            throw new IllegalStateException("Cannot new a plugin at runtime");
        if (!getDefaultFolder().exists())
            if (!getDefaultFolder().mkdirs())
                FocessQQ.getLogger().debugLang("create-default-folder-failed",getDefaultFolder().getAbsolutePath());
        config = new File(getDefaultFolder(), "config.yml");
        if (!config.exists()) {
            try {
                InputStream configResource = loadResource("config.yml");
                if (configResource != null) {
                    Files.copy(configResource, config.toPath());
                    configResource.close();
                } else if (!config.createNewFile())
                    FocessQQ.getLogger().debugLang("create-config-file-failed", config.getAbsolutePath());
            } catch (IOException e) {
                FocessQQ.getLogger().thrLang("exception-create-config-file",e);
            }
        }
        configuration = YamlConfiguration.loadFile(config);
        defaultConfig = new DefaultConfig(config);
        langConfig = new LangConfig(loadResource("lang.yml"));
    }

    /**
     * Get Plugin instance by the class instance
     *
     * @see PluginClassLoader#getPlugin(Class)
     * @param plugin the class instance of the plugin
     * @return the plugin instance
     */
    public static Plugin getPlugin(Class<? extends Plugin> plugin) {
        return PluginClassLoader.getPlugin(plugin);
    }

    /**
     * Get Plugin instance by the name
     *
     * @see PluginClassLoader#getPlugin(String)
     * @param name the name of the plugin
     * @return the plugin instance
     */
    public static Plugin getPlugin(String name) {
        return PluginClassLoader.getPlugin(name);
    }

    @Nullable
    public static Plugin thisPlugin() {
        return PluginCoreClassLoader.getClassLoadedBy(MethodCaller.getCallerClass());
    }

    @NotNull
    public String getName() {
        return name;
    }

    /**
     * Used to initialize the plugin
     */
    public abstract void enable();

    /**
     * Used to save some data of the plugin
     */
    public abstract void disable();

    public final void onEnable() {
        this.isEnabled = true;
        this.enable();
    }

    public final void onDisable() {
        this.disable();
        this.isEnabled = false;
    }

    @NotNull
    public File getDefaultFolder() {
        return new File(new File(new File(path).getParent(), "plugins"), this.getName());
    }

    @NotNull
    @Deprecated
    public File getConfigFile() {
        return config;
    }

    @NotNull
    @Deprecated
    public YamlConfiguration getConfig() {
        return configuration;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Plugin plugin = (Plugin) o;

        return Objects.equals(name, plugin.name);
    }

    @Override
    public int hashCode() {
        return name != null ? name.hashCode() : 0;
    }

    /**
     * Register the listener into the Event chain
     *
     * @param listener the listener need to be registered
     */
    public void registerListener(Listener listener) {
        ListenerHandler.register(this, listener);
        for (Method method : listener.getClass().getDeclaredMethods()) {
            EventHandler handler;
            if ((handler = method.getAnnotation(EventHandler.class)) != null) {
                if (method.getParameterTypes().length == 1) {
                    Class<?> eventClass = method.getParameterTypes()[0];
                    if (Event.class.isAssignableFrom(eventClass) && !Modifier.isAbstract(eventClass.getModifiers())) {
                        try {
                            Field field = eventClass.getDeclaredField("LISTENER_HANDLER");
                            boolean flag = field.isAccessible();
                            field.setAccessible(true);
                            ListenerHandler listenerHandler = (ListenerHandler) field.get(null);
                            field.setAccessible(flag);
                            listenerHandler.register(listener, method, handler);
                        } catch (Exception ignored) {
                        }
                    }
                }
            }
        }
    }

    /**
     * Register the command
     * @param command the command need to be registered
     */
    public void registerCommand(Command command) {
        Command.register(this,command);
    }

    /**
     * Register the getter of the buffer
     *
     * @param dataConverter the buffer data converter
     * @param bufferGetter the getter of the buffer
     */
    public void registerBuffer(DataConverter<?> dataConverter, DataCollection.BufferGetter bufferGetter) {
        DataCollection.register(this,dataConverter,bufferGetter);
    }

    public String getAuthor() {
        return author;
    }

    public Version getVersion() {
        return version;
    }

    public LangConfig getLangConfig() {
        return langConfig;
    }

    public DefaultConfig getDefaultConfig() {
        return defaultConfig;
    }

    public PluginDescription getPluginDescription() {
        return pluginDescription;
    }

    public InputStream loadResource(String path) {
        return this.getClass().getClassLoader().getResourceAsStream(path);
    }

    public boolean isEnabled() {
        return isEnabled;
    }
}
