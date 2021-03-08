package com.focess.commands;

import com.focess.api.Plugin;
import com.focess.api.annotation.CommandType;
import com.focess.api.annotation.PluginType;
import com.focess.api.exception.IllegalCommandClassException;
import com.focess.api.exception.IllegalPluginClassException;
import com.focess.api.command.Command;
import com.focess.api.command.CommandResult;
import com.focess.api.command.CommandSender;
import com.focess.api.util.IOHandler;
import com.focess.commands.util.AnnotationHandler;
import com.focess.commands.util.ChatConstants;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import java.io.File;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Modifier;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class LoadCommand extends Command {
    static List<Plugin> registeredPlugins = Lists.newCopyOnWriteArrayList();

    public LoadCommand() {
        super("load", Lists.newArrayList());
    }

    public static Plugin getPlugin(Class<? extends Plugin> plugin) {
        for (Plugin p:registeredPlugins)
            if (p.getClass().isAssignableFrom(plugin))
                return p;
        return null;
    }

    public static List<Plugin> getPlugins() {
        return registeredPlugins;
    }

    public static void addPlugin(Plugin plugin) {
        registeredPlugins.add(plugin);
    }

    public static void removePlugin(Plugin plugin){
        registeredPlugins.remove(plugin);
    }

    public static Plugin getPlugin(String name) {
        for (Plugin plugin:registeredPlugins)
            if (plugin.getName().equals(name))
                return plugin;
            return null;
    }

    @Override
    public void init() {
        this.addExecutor(1, (sender, data, ioHandler) ->{
            if (sender.isConsole()) {
                ioHandler.output(ChatConstants.CONSOLE_HEADER + "Start loading...");
                String path = data.get();
                File file = new File(path);
                if (file.exists() && file.getName().endsWith(".jar")) {
                    try {
                        ioHandler.output(ChatConstants.CONSOLE_HEADER + "Start PluginClassLoader");
                        PluginClassLoader classLoader = new PluginClassLoader(file, this.getClass().getClassLoader());
                        classLoader.load();
                        classLoader.close();
                        ioHandler.output(ChatConstants.CONSOLE_HEADER + "End PluginClassLoader");
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    ioHandler.output(ChatConstants.CONSOLE_HEADER + "End loading...");
                    return CommandResult.ALLOW;
                }
                ioHandler.output(ChatConstants.CONSOLE_HEADER + "File is not existed.");
                ioHandler.output(ChatConstants.CONSOLE_HEADER + "End loading...");
                return CommandResult.REFUSE;
            }
            ioHandler.output(ChatConstants.CONSOLE_HEADER + "Permission denied.");
            return CommandResult.REFUSE;
        });
    }

    @Override
    public void usage(CommandSender commandSender, IOHandler ioHandler) {
        ioHandler.output("Use: load [jar-path]");
    }

    public static class PluginClassLoader extends URLClassLoader {

        private File file;

        private static Map<Class<? extends Annotation>,AnnotationHandler> handlers = Maps.newHashMap();

        private static final AnnotationHandler PLUGIN_TYPE_HANDLER = (c,annotation,classLoader)->{
            if (Plugin.class.isAssignableFrom(c) && !Modifier.isAbstract(c.getModifiers())) {
                try {
                    classLoader.addPlugin((Plugin) c.newInstance());
                } catch (InstantiationException e) {
                    e.printStackTrace();
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
            else throw new IllegalPluginClassException();
        };

        static {
            handlers.put(CommandType.class,(c,annotation,classLoader) ->{
                CommandType commandType = (CommandType) annotation;
                if (commandType.plugin() == null)
                    throw new IllegalArgumentException();
                if (Command.class.isAssignableFrom(c) && !Modifier.isAbstract(c.getModifiers())) {
                    try {
                        Command.register(getPlugin(commandType.plugin()),(Command)c.newInstance());
                    } catch (InstantiationException e) {
                        e.printStackTrace();
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    }
                }
                else throw new IllegalCommandClassException();
            });
        }

        private List<Plugin> plugins = Lists.newArrayList();

        private boolean addPlugin(Plugin plugin) {
            if (plugin == null)
                return false;
            for (Plugin p:plugins)
                if (p.getName().equals(p.getName()))
                    return false;
            for (Plugin p:registeredPlugins)
                if (p.getName().equals(plugin.getName()))
                    return false;
            plugins.add(plugin);
            return true;
        }

        public PluginClassLoader(File file, ClassLoader parent) throws MalformedURLException {
            super(new URL[]{file.toURI().toURL()},parent);
            this.file = file;
        }

        private final List<Class> loadedClasses = Lists.newArrayList();

        public void load() {
            try {
                JarFile jarFile = new JarFile(file);
                Enumeration<JarEntry> entries = jarFile.entries();
                while (entries.hasMoreElements()) {
                    JarEntry jarEntry = entries.nextElement();
                    String name = jarEntry.getName();
                    if (!name.endsWith(".class"))
                        continue;
                    Class c = this.loadClass(name.replace("/", ".").substring(0,name.length() - 6));
                    loadedClasses.add(c);
                    analyseClass0(c);
                }
                for (Plugin plugin:plugins) {
                    registeredPlugins.add(plugin);
                    plugin.enable();
                }
                for (Class c:loadedClasses)
                    analyseClass(c);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        private void analyseClass0(Class c) {
            Annotation annotation;
            if ((annotation = c.getAnnotation(PluginType.class)) != null)
                PLUGIN_TYPE_HANDLER.handle(c,annotation,this);
        }

        private <T extends Annotation> void analyseClass(Class c)  {
            for (Class<? extends Annotation> annotation:handlers.keySet()) {
                Annotation a;
                if ((a = c.getAnnotation(annotation)) != null)
                    handlers.get(annotation).handle(c, a, this);
            }
        }


    }
}
