package top.focess.qq.core.plugin;

import com.google.common.collect.Lists;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import top.focess.qq.FocessQQ;
import top.focess.qq.api.plugin.Plugin;

import java.util.List;

public class PluginCoreClassLoader extends ClassLoader {

    static final PluginCoreClassLoader DEFAULT_CLASS_LOADER = new PluginCoreClassLoader(PluginCoreClassLoader.class.getClassLoader());
    static final List<PluginClassLoader> LOADERS = Lists.newCopyOnWriteArrayList();

    public PluginCoreClassLoader(final ClassLoader parent) {
        super(parent);
    }

    public static Class<?> forName(final String name) throws ClassNotFoundException {
        return DEFAULT_CLASS_LOADER.loadClass(name, false);
    }

    @Nullable
    public static Plugin getPluginByClass(@Nullable final Class<?> clazz) {
        if (clazz == null)
            return null;
        if (clazz.getClassLoader() instanceof PluginClassLoader)
            for (final PluginClassLoader pluginClassLoader : LOADERS)
                if (pluginClassLoader.getLoadedClasses().contains(clazz))
                    return pluginClassLoader.getPlugin();
        return null;
    }

    @NonNull
    public static Plugin getPluginByClassOrDefault(@Nullable final Class<?> clazz) {
        final Plugin plugin = getPluginByClass(clazz);
        if (plugin == null)
            return FocessQQ.getMainPlugin();
        return plugin;
    }

    @Override
    public Class<?> loadClass(final String name, final boolean resolve) throws ClassNotFoundException {
        try {
            return super.loadClass(name, resolve);
        } catch (final ClassNotFoundException e) {
            for (final PluginClassLoader classLoader : LOADERS)
                try {
                    return classLoader.findClass(name, resolve);
                } catch (final ClassNotFoundException ignored) {
                }
        }
        throw new ClassNotFoundException(name);
    }
}