package com.focess.commands;

import com.focess.api.Plugin;
import com.focess.api.annotation.CommandType;
import com.focess.api.annotation.PluginType;
import com.focess.api.exception.CommandDuplicateException;
import com.focess.api.exception.IllegalCommandClassException;
import com.focess.api.exception.IllegalPluginClassException;
import com.focess.api.command.Command;
import com.focess.api.command.CommandResult;
import com.focess.api.command.CommandSender;
import com.focess.api.exception.PluginDuplicateException;
import com.focess.api.util.IOHandler;
import com.focess.commands.util.AnnotationHandler;
import com.focess.commands.util.ChatConstants;
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
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class LoadCommand extends Command {
    private static final List<Plugin> registeredPlugins = Lists.newCopyOnWriteArrayList();

    public LoadCommand() {
        super("load", Lists.newArrayList());
    }

    @Nullable
    public static Plugin getPlugin(Class<? extends Plugin> plugin) {
        for (Plugin p:registeredPlugins)
            if (p.getClass().equals(plugin))
                return p;
        return null;
    }

    @NonNull
    public static List<Plugin> getPlugins() {
        return registeredPlugins;
    }

    public static void loadPlugin(Plugin plugin) {
        if (getPlugin(plugin.getClass()) != null)
            throw new PluginDuplicateException(plugin.getName());
        registeredPlugins.add(plugin);
        plugin.enable();
    }

    public static void disablePlugin(Plugin plugin){
        plugin.disable();
        Command.unregister(plugin);
        registeredPlugins.remove(plugin);
    }

    @Nullable
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
                        if (!classLoader.load())
                            ioHandler.output(ChatConstants.CONSOLE_HEADER + "Plugin need load after some other plugins.");
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
            return CommandResult.REFUSE;
        });
    }

    @Override
    public void usage(CommandSender commandSender, IOHandler ioHandler) {
        ioHandler.output("Use: load [jar-path]");
    }

    public static class PluginClassLoader extends URLClassLoader {

        private static final Map<String,Set<File>> afterPlugins = Maps.newHashMap();

        private static final Set<File> afterPluginFiles = Sets.newConcurrentHashSet();

        private final File file;

        private static final Map<Class<? extends Annotation>,AnnotationHandler> handlers = Maps.newHashMap();

        private static final AnnotationHandler PLUGIN_TYPE_HANDLER = (c,annotation,classLoader)->{
            PluginType pluginType = (PluginType) annotation;
            if (!pluginType.loadAfter().equals("") && getPlugin(pluginType.loadAfter()) == null) {
                afterPlugins.compute(pluginType.loadAfter(),(key,value)->{
                    if (value == null)
                        return Sets.newHashSet(classLoader.file);
                    else value.add(classLoader.file);
                    return value;
                });
                afterPluginFiles.add(classLoader.file);
                return false;
            }
            if (Plugin.class.isAssignableFrom(c) && !Modifier.isAbstract(c.getModifiers())) {
                try {
                    Plugin plugin;
                    if (!classLoader.addPlugin( plugin = (Plugin) c.newInstance()))
                        throw new PluginDuplicateException(plugin.getName());
                    return true;
                } catch (InstantiationException | IllegalAccessException e) {
                    e.printStackTrace();
                    return false;
                }
            }
            else throw new IllegalPluginClassException();
        };

        static {
            handlers.put(CommandType.class,(c,annotation,classLoader) ->{
                CommandType commandType = (CommandType) annotation;
                if (Command.class.isAssignableFrom(c) && !Modifier.isAbstract(c.getModifiers())) {
                    try {
                        //NullPointer maybe thrown
                        Command command;
                        if (!Command.register(Objects.requireNonNull(getPlugin(commandType.plugin())),command = (Command)c.newInstance()))
                            throw new CommandDuplicateException(command.getName());
                    } catch (InstantiationException | IllegalAccessException e) {
                        e.printStackTrace();
                    }
                }
                else throw new IllegalCommandClassException();
                return true;
            });
        }

        private final List<Plugin> plugins = Lists.newArrayList();

        private boolean addPlugin(@NonNull Plugin plugin) {
            for (Plugin p:plugins)
                if (p.getName().equals(plugin.getName()))
                    return false;
            for (Plugin p:registeredPlugins)
                if (p.getName().equals(plugin.getName()))
                    return false;
            plugins.add(plugin);
            return true;
        }

        public PluginClassLoader(@NonNull File file,ClassLoader parent) throws MalformedURLException {
            super(new URL[]{file.toURI().toURL()},parent);
            this.file = file;
        }

        private final List<Class<?>> loadedClasses = Lists.newArrayList();

        public boolean load() {
            try {
                JarFile jarFile = new JarFile(file);
                Enumeration<JarEntry> entries = jarFile.entries();
                while (entries.hasMoreElements()) {
                    JarEntry jarEntry = entries.nextElement();
                    String name = jarEntry.getName();
                    if (!name.endsWith(".class"))
                        continue;
                    Class<?> c = this.loadClass(name.replace("/", ".").substring(0,name.length() - 6));
                    loadedClasses.add(c);
                    if (!analyseClass0(c))
                        return false;
                }
                for (Plugin plugin:plugins)
                    loadPlugin(plugin);
                for (Class<?> c:loadedClasses)
                    analyseClass(c);
                for (Plugin plugin:plugins) {
                    for (File file : afterPlugins.getOrDefault(plugin.getName(), Sets.newHashSet())) {
                        if (afterPluginFiles.contains(file)) {
                            PluginClassLoader pluginClassLoader = new PluginClassLoader(file,new ParentClassLoader(this,this.getParent()) );
                            if (pluginClassLoader.load()) {
                                CommandSender.CONSOLE.getIOHandler().output("Successfully load some other plugins after " + plugin.getName() + ".");
                                afterPluginFiles.remove(file);
                            }
                            pluginClassLoader.close();
                        }
                    }
                    afterPlugins.remove(plugin.getName());
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return true;
        }

        @Override
        public Class<?> loadClass(String name) throws ClassNotFoundException {
            return super.loadClass(name);
        }

        private boolean analyseClass0(@NonNull Class<?> c) {
            Annotation annotation;
            if ((annotation = c.getAnnotation(PluginType.class)) != null)
                return PLUGIN_TYPE_HANDLER.handle(c, annotation, this);
            return true;
        }

        private <T extends Annotation> void analyseClass(@NonNull Class<?> c)  {
            for (Class<? extends Annotation> annotation:handlers.keySet()) {
                Annotation a;
                if ((a = c.getAnnotation(annotation)) != null)
                    handlers.get(annotation).handle(c, a, this);
            }
        }

        private static class ParentClassLoader extends ClassLoader {
            private final ClassLoader classLoader;
            private final ClassLoader parent;

            public ParentClassLoader(ClassLoader classLoader, ClassLoader parent) {
                this.classLoader = classLoader;
                this.parent = parent;
            }

            @Override
            public Class<?> loadClass(String name) throws ClassNotFoundException {
                Class<?> cls;
                try {
                    cls = this.classLoader.loadClass(name);
                } catch (Exception e) {
                    cls = this.parent.loadClass(name);
                }
                return cls;
            }

            @Override
            protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
                Class<?> cls = loadClass(name);
                if (resolve)
                    this.resolveClass(cls);
                return cls;
            }
        }

    }
}
