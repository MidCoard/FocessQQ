package top.focess.qq.api.plugin;

import com.google.common.collect.Maps;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.UnmodifiableView;
import top.focess.command.DataConverter;
import top.focess.qq.FocessQQ;
import top.focess.qq.api.command.Command;
import top.focess.qq.api.command.CommandLine;
import top.focess.qq.api.command.DataCollection;
import top.focess.qq.api.command.SpecialArgumentComplexHandler;
import top.focess.qq.api.event.Listener;
import top.focess.qq.api.event.ListenerHandler;
import top.focess.qq.api.util.config.DefaultConfig;
import top.focess.qq.api.util.config.LangConfig;
import top.focess.qq.core.plugin.PluginClassLoader;
import top.focess.qq.core.plugin.PluginCoreClassLoader;
import top.focess.qq.core.util.MethodCaller;
import top.focess.util.serialize.FocessSerializable;
import top.focess.util.version.Version;
import top.focess.util.yaml.YamlConfiguration;
import top.focess.util.yaml.YamlLoadException;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Represent a Plugin class that can be load, enable and disable. Also, provide plenty of API for the plugin to get better with this framework.
 * You should declare {@link PluginType} to this class.
 */
public abstract class Plugin implements FocessSerializable {

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
     * Indicate the plugin is enabled or not
     */
    private boolean isEnabled;


    private boolean initialized;

    /**
     * Initialize a Plugin Instance.
     * Never instance it! It will be instanced when bot bootstraps automatically.
     *
     * @throws PluginLoaderException if the classloader of the plugin is not {@link PluginClassLoader}
     * @throws PluginDuplicateException if the plugin is already loaded
     * @throws IllegalStateException if the plugin is newed in runtime
     * @throws IllegalArgumentException if the plugin name is empty
     */
    public Plugin() {
        this.initialize();
    }
    /**
     * Get all the loaded plugins
     *
     * @return all the loaded plugins
     */
    @NotNull
    public static @UnmodifiableView List<Plugin> getPlugins() {
        return PluginClassLoader.getPlugins();
    }

    /**
     * Get Plugin instance by the class instance
     *
     * @param plugin the class instance of the plugin
     * @return the plugin instance
     */
    @Nullable
    public static Plugin getPlugin(final Class<? extends Plugin> plugin) {
        return PluginClassLoader.getPlugin(plugin);
    }

    /**
     * Get Plugin instance by the name
     *
     * @param name the name of the plugin
     * @return the plugin instance
     */
    @Nullable
    public static Plugin getPlugin(final String name) {
        return PluginClassLoader.getPlugin(name);
    }

    /**
     * Get the plugin by its caller class
     *
     * @return the plugin or null if not found
     */
    @Nullable
    public static Plugin thisPlugin() {
        return PluginCoreClassLoader.getClassLoadedBy(MethodCaller.getCallerClass());
    }

    /**
     * Get the plugin by its caller class
     *
     * Note: this method will not return null. If the {@link Plugin#thisPlugin()} is null, it will return {@link FocessQQ#getMainPlugin()}
     *
     * @return the plugin
     */
    @NonNull
    public static Plugin plugin() {
        final Plugin plugin = PluginCoreClassLoader.getClassLoadedBy(MethodCaller.getCallerClass());
        return plugin == null ? FocessQQ.getMainPlugin() : plugin;
    }


    @NonNull
    public final String getName() {
        return this.name;
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

    @NonNull
    public final File getDefaultFolder() {
        return new File(new File("plugins"), this.getName());
    }

    @Override
    public boolean equals(@Nullable final Object o) {
        if (this == o) return true;
        if (o == null || this.getClass() != o.getClass()) return false;

        final Plugin plugin = (Plugin) o;

        return Objects.equals(this.name, plugin.name);
    }

    @Override
    public int hashCode() {
        return this.name != null ? this.name.hashCode() : 0;
    }

    /**
     * Register the listener into the Event chain
     *
     * @param listener the listener need to be registered
     */
    public final void registerListener(final Listener listener) {
        ListenerHandler.register(this, listener);
    }

    /**
     * Register the command
     *
     * @param command the command need to be registered
     * @see Command#register(Plugin, Command)
     */
    public final void registerCommand(final Command command) {
        Command.register(this, command);
    }

    /**
     * Register the getter of the buffer
     *
     * @param dataConverter the buffer data converter
     * @param bufferGetter  the getter of the buffer
     * @see DataCollection#register(Plugin, DataConverter, top.focess.command.DataCollection.BufferGetter)
     */
    public final void registerBuffer(final DataConverter<?> dataConverter, final top.focess.command.DataCollection.BufferGetter bufferGetter) {
        DataCollection.register(this, dataConverter, bufferGetter);
    }

    public final String getAuthor() {
        return this.author;
    }

    public final Version getVersion() {
        return this.version;
    }

    public final LangConfig getLangConfig() {
        return this.langConfig;
    }

    public final DefaultConfig getDefaultConfig() {
        return this.defaultConfig;
    }

    public final PluginDescription getPluginDescription() {
        return this.pluginDescription;
    }

    /**
     * Get the resource of the plugin
     * @param path the path of the resource
     * @return the resource or null if not found
     */
    @Nullable
    public final InputStream loadResource(final String path) {
        return this.getClass().getClassLoader().getResourceAsStream(path);
    }

    public final boolean isEnabled() {
        return this.isEnabled;
    }

    /**
     * Used to unload this plugin during enabling process
     *
     * This should be called in the {@link #enable()} method
     *
     * @throws PluginUnloadException to indicate that the plugin should be unloaded
     */
    public final void unload() {
        throw new PluginUnloadException();
    }

    /**
     * Register the special argument handler
     *
     * @param name    the name of the special argument handler
     * @param handler the special argument handler
     * @see CommandLine#register(Plugin, String, SpecialArgumentComplexHandler)
     */
    public final void registerSpecialArgumentComplexHandler(final String name, final SpecialArgumentComplexHandler handler) {
        CommandLine.register(this, name, handler);
    }

    @Nullable
    @Override
    public Map<String, Object> serialize() {
        final Map<String,Object> map = Maps.newHashMap();
        map.put("name",this.name);
        return map;
    }

    @Nullable
    public static Plugin deserialize(final Map<String,Object> map){
        return getPlugin((String) map.get("name"));
    }

    /**
     * Get the plugin jar file
     * @return the plugin jar file or null if it is not loaded from PluginClassLoader (the MainPlugin)
     */
    @Nullable
    public File getFile() {
        if (this.getClass().getClassLoader() instanceof PluginClassLoader)
            return ((PluginClassLoader) this.getClass().getClassLoader()).getFile();
        else return null;
    }

    /**
     * Indicate whether the plugin is initialized or not
     * @return true if the plugin is initialized, false otherwise
     */
    public boolean isInitialized() {
        return this.initialized;
    }

    public void initialize() {
        if (this.getClass() != FocessQQ.MainPlugin.class || FocessQQ.isRunning()) {
            this.pluginDescription = new PluginDescription(YamlConfiguration.load(this.loadResource("plugin.yml")));
            this.name = this.pluginDescription.getName();
            this.author = this.pluginDescription.getAuthor();
            this.version = this.pluginDescription.getVersion();
            if (getPlugin(this.getClass()) != null)
                throw new PluginDuplicateException(this.name, "Cannot new a plugin at runtime");
        } else {
            this.pluginDescription = new PluginDescription();
            this.name = this.pluginDescription.getName();
            this.author = this.pluginDescription.getAuthor();
            this.version = this.pluginDescription.getVersion();
        }
        if (this.name.isEmpty())
            throw new IllegalArgumentException("Plugin name cannot be empty");
        if (!(this.getClass().getClassLoader() instanceof PluginClassLoader) && this.getClass() != FocessQQ.MainPlugin.class)
            throw new PluginLoaderException(this.name);
        if (FocessQQ.getVersion().lower(this.pluginDescription.getRequireVersion()) || !this.pluginDescription.getLimitVersion().equals(FocessQQ.getVersion()))
            throw new IllegalStateException("Version limitation not satisfied");
        if (!this.getClass().getName().equals(this.pluginDescription.getMain()))
            throw new IllegalStateException("Cannot new a plugin at runtime");
        if (!this.getDefaultFolder().exists())
            if (!this.getDefaultFolder().mkdirs())
                FocessQQ.getLogger().debugLang("create-default-folder-failed", this.getDefaultFolder().getAbsolutePath());
        final File config = new File(this.getDefaultFolder(), "config.yml");
        if (!config.exists()) {
            try {
                final InputStream configResource = this.loadResource("config.yml");
                if (configResource != null) {
                    Files.copy(configResource, config.toPath());
                    configResource.close();
                } else if (!config.createNewFile())
                    FocessQQ.getLogger().debugLang("create-config-file-failed", config.getAbsolutePath());
            } catch (final IOException e) {
                FocessQQ.getLogger().thrLang("exception-create-config-file", e);
            }
        }
        try {
            this.defaultConfig = new DefaultConfig(config);
        } catch (final YamlLoadException e) {
            FocessQQ.getLogger().thrLang("exception-load-default-configuration", e);
        }
        this.langConfig = new LangConfig(this.loadResource("lang.yml"));
        this.initialized = true;
    }
}
