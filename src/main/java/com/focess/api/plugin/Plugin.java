package com.focess.api.plugin;

import com.focess.Main;
import com.focess.api.annotation.EventHandler;
import com.focess.api.event.Event;
import com.focess.api.event.Listener;
import com.focess.api.event.ListenerHandler;
import com.focess.api.exceptions.PluginLoaderException;
import com.focess.api.util.config.DefaultConfig;
import com.focess.api.util.config.LangConfig;
import com.focess.api.util.version.Version;
import com.focess.api.util.yaml.YamlConfiguration;
import com.focess.core.plugin.PluginClassLoader;
import com.focess.core.plugin.PluginCoreClassLoader;
import com.focess.core.util.MethodCaller;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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
 * You should declare {@link com.focess.api.annotation.PluginType} to this class.
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
     * Initialize a Plugin instance by its name.
     * Never instance it! It will be instanced when bot bootstraps automatically.
     *
     * @param name the plugin name
     * @param author the plugin author
     * @param version the plugin version
     * @throws PluginLoaderException if the classloader of the plugin is not {@link PluginClassLoader}
     */
    public Plugin(String name, String author, Version version) {
        if (!(this.getClass().getClassLoader() instanceof PluginClassLoader) && this.getClass() != Main.MainPlugin.class)
            throw new PluginLoaderException(name);
        this.name = name;
        this.author = author;
        this.version = version;
        if (!getDefaultFolder().exists())
            if (!getDefaultFolder().mkdirs())
                Main.getLogger().debug("Create Default Folder Failed");
        config = new File(getDefaultFolder(), "config.yml");
        if (!config.exists()) {
            try {
                InputStream configResource = loadResource("config.yml");
                if (configResource != null) {
                    Files.copy(configResource, config.toPath());
                    configResource.close();
                } else if (!config.createNewFile())
                    Main.getLogger().debug("Create Default Config File Failed");
            } catch (IOException e) {
                Main.getLogger().thr("Create Config File Exception",e);
            }
        }
        configuration = YamlConfiguration.loadFile(config);
        defaultConfig = new DefaultConfig(config);
        File lang = new File(getDefaultFolder(), "lang.yml");
        if (!lang.exists()) {
            try {
                InputStream configResource = loadResource("config.yml");
                if (configResource != null) {
                    Files.copy(configResource, config.toPath());
                    configResource.close();
                }
            } catch (IOException e) {
                Main.getLogger().thr("Create Lang File Exception",e);
            }
        }
        langConfig = new LangConfig(lang);
    }

    /**
     * Provide a constructor to help {@link com.focess.api.annotation.PluginType} design.
     * Never instance it! It will be instanced when bot bootstraps automatically.
     */
    protected Plugin() {
        if (!(this.getClass().getClassLoader() instanceof PluginClassLoader))
            throw new PluginLoaderException(this.getClass());
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

    @NotNull
    public File getDefaultFolder() {
        return new File(new File(new File(path).getParent(), "plugins"), this.getName());
    }

    @NotNull
    public File getConfigFile() {
        return config;
    }

    @NotNull
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
}
