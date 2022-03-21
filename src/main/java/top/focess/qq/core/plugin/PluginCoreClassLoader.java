package top.focess.qq.core.plugin;

import com.google.common.collect.Lists;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import top.focess.qq.FocessQQ;
import top.focess.qq.api.plugin.Plugin;

import java.util.List;

public class PluginCoreClassLoader extends ClassLoader {

    public static final PluginCoreClassLoader DEFAULT_CLASS_LOADER = new PluginCoreClassLoader(PluginCoreClassLoader.class.getClassLoader());
    public static final List<PluginClassLoader> LOADERS = Lists.newCopyOnWriteArrayList();

    public PluginCoreClassLoader(ClassLoader parent) {
        super(parent);
    }

    public static Class<?> forName(String name) throws ClassNotFoundException {
        return DEFAULT_CLASS_LOADER.loadClass(name, false);
    }

    /**
     * Get the plugin of the loaded class
     *
     * @param clazz the class
     * @return the target plugin, @null if the class is loaded by default classloader
     */
    @Nullable
    public static Plugin getClassLoadedBy(@Nullable Class<?> clazz) {
        if (clazz == null)
            return null;
        if (clazz.getClassLoader() instanceof PluginClassLoader)
            for (PluginClassLoader pluginClassLoader : LOADERS)
                if (pluginClassLoader.getLoadedClasses().contains(clazz))
                    return pluginClassLoader.getPlugin();
        return null;
    }

    /**
     * Get the plugin of the loaded class
     *
     * @param clazz the class
     * @return the target plugin, {@code FocessQQ#getMainPlugin()} if the class is loaded by default classloader
     */
    @NonNull
    public static Plugin getClassLoadedByOrDefault(@Nullable  Class<?> clazz) {
        Plugin plugin = getClassLoadedBy(clazz);
        if (plugin == null)
            return FocessQQ.getMainPlugin();
        return plugin;
    }

    @Override
    public Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
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