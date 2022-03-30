package top.focess.qq.core.schedule;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import top.focess.qq.FocessQQ;
import top.focess.qq.api.plugin.Plugin;
import top.focess.qq.api.schedule.Scheduler;

import java.util.List;
import java.util.Map;

public abstract class AScheduler implements Scheduler {

    private static final Map<Plugin, List<Scheduler>> PLUGIN_SCHEDULER_MAP = Maps.newConcurrentMap();
    private final Plugin plugin;

    public AScheduler(final Plugin plugin) {
        this.plugin = plugin;
        PLUGIN_SCHEDULER_MAP.compute(plugin, (k, v) -> {
            if (v == null)
                v = Lists.newCopyOnWriteArrayList();
            v.add(this);
            return v;
        });
    }

    /**
     * Close all the schedulers belonging to the plugin
     *
     * @param plugin the plugin
     */
    public static void close(final Plugin plugin) {
        for (final Scheduler scheduler : PLUGIN_SCHEDULER_MAP.getOrDefault(plugin, Lists.newCopyOnWriteArrayList()))
            scheduler.close();
        PLUGIN_SCHEDULER_MAP.remove(plugin);
    }

    /**
     * Close all the schedulers
     *
     * @return true if there are some schedulers not belonging to MainPlugin not been closed, false otherwise
     */
    public static boolean closeAll() {
        boolean ret = false;
        for (final Plugin plugin : PLUGIN_SCHEDULER_MAP.keySet()) {
            if (plugin != FocessQQ.getMainPlugin())
                ret = true;
            close(plugin);
        }
        return ret;
    }

    @Override
    public Plugin getPlugin() {
        return this.plugin;
    }

    @Override
    public void close() {
        PLUGIN_SCHEDULER_MAP.compute(this.plugin, (k, v) -> {
            if (v != null)
                v.remove(this);
            return v;
        });
    }
}
