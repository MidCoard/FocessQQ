package top.focess.qq.api.command;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import top.focess.command.DataConverter;
import top.focess.qq.FocessQQ;
import top.focess.qq.api.plugin.Plugin;
import top.focess.qq.core.permission.Permission;

import java.util.List;
import java.util.Map;

/**
 * Store and parser arguments for better CommandExecutor usage.
 */
public class DataCollection {

    private static final Map<Plugin, List<DataConverter<?>>> PLUGIN_DATA_CONVERTER_MAP = Maps.newConcurrentMap();

    /**
     * Register the getter of the buffer
     *
     * @param plugin        the plugin
     * @param dataConverter the buffer data converter
     * @param bufferGetter  the getter of the buffer
     */
    public static void register(final Plugin plugin, final DataConverter<?> dataConverter, final top.focess.command.DataCollection.BufferGetter bufferGetter) {
        Permission.checkPermission(Permission.REGISTER_DATA_BUFFER);
        PLUGIN_DATA_CONVERTER_MAP.compute(plugin, (k, v) -> {
            if (v == null)
                v = Lists.newArrayList();
            v.add(dataConverter);
            return v;
        });
        top.focess.command.DataCollection.register(dataConverter, bufferGetter);
    }

    /**
     * Unregister the getter of the buffer
     * @param dataConverter the data converter
     */
    public static void unregister(final DataConverter<?> dataConverter) {
        Permission.checkPermission(Permission.REMOVE_DATA_BUFFER);
        top.focess.command.DataCollection.unregister(dataConverter);
    }

    /**
     * Unregister the getter of the buffers by plugin
     *
     * @param plugin the plugin
     */
    public static void unregister(final Plugin plugin) {
        Permission.checkPermission(Permission.REMOVE_DATA_BUFFER);
        for (final DataConverter<?> dataConverter : PLUGIN_DATA_CONVERTER_MAP.getOrDefault(plugin, Lists.newArrayList()))
             unregister(dataConverter);
        PLUGIN_DATA_CONVERTER_MAP.remove(plugin);
    }

    /**
     * Unregister all the getter of the buffers
     *
     * @return true if there are some getter of the buffers not belonging to MainPlugin not been unregistered, false otherwise
     */
    public static boolean unregisterAll() {
        Permission.checkPermission(Permission.REMOVE_DATA_BUFFER);
        boolean ret = false;
        for (final Plugin plugin : PLUGIN_DATA_CONVERTER_MAP.keySet()) {
            if (plugin != FocessQQ.getMainPlugin())
                ret = true;
            unregister(plugin);
        }
        PLUGIN_DATA_CONVERTER_MAP.clear();
        return ret;
    }
}
