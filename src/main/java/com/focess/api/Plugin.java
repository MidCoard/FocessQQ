package com.focess.api;

import com.focess.api.annotation.EventHandler;
import com.focess.api.event.Event;
import com.focess.api.event.Listener;
import com.focess.api.event.ListenerHandler;
import com.focess.commands.LoadCommand;
import com.focess.util.yaml.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

public abstract class Plugin {

    private static final String path = Plugin.class.getProtectionDomain().getCodeSource().getLocation().getFile();
    private final String name;
    private final YamlConfiguration configuration;
    private final File config;

    public Plugin(String name) {
        this.name = name;
        if (!getDefaultFolder().exists())
            getDefaultFolder().mkdirs();
        config = new File(getDefaultFolder(), "config.yml");
        if (!config.exists()) {
            try {
                config.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        configuration = YamlConfiguration.loadFile(getConfigFile());
    }

    public static Plugin getPlugin(Class<? extends Plugin> plugin) {
        return LoadCommand.getPlugin(plugin);
    }

    public static Plugin getPlugin(String name) {
        return LoadCommand.getPlugin(name);
    }

    public String getName() {
        return name;
    }

    public abstract void enable();

    public abstract void disable();

    public File getDefaultFolder() {
        return new File(new File(new File(path).getParent(), "plugins"), this.getName());
    }

    public File getConfigFile() {
        return config;
    }

    public YamlConfiguration getConfig() {
        return configuration;
    }

    public void registerListener(Listener listener) {
        ListenerHandler.addListener(this, listener);
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
                            listenerHandler.addListener(listener, method, handler);
                        } catch (Exception ignored) {
                        }
                    }
                }
            }
        }
    }
}
