package top.focess.qq.api.schedule;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.jetbrains.annotations.NotNull;
import top.focess.qq.Main;
import top.focess.qq.api.plugin.Plugin;
import top.focess.qq.core.schedule.FocessScheduler;
import top.focess.qq.core.schedule.ThreadPoolScheduler;

import java.util.List;
import java.util.Map;

/**
 * Used to create Scheduler. The scheduler factory.
 */
public class Schedulers {

    private static final Map<Plugin, List<Scheduler>> PLUGIN_SCHEDULER_MAP = Maps.newHashMap();

    private Schedulers() {}

    /**
     * New a FocessScheduler
     *
     * @param plugin the plugin
     * @return a FocessScheduler
     */
    public static Scheduler newFocessScheduler(@NotNull Plugin plugin) {
        Scheduler scheduler = new FocessScheduler(plugin);
        PLUGIN_SCHEDULER_MAP.compute(plugin,(k,v)->{
            if (v == null)
                v = Lists.newArrayList();
            v.add(scheduler);
            return v;
        });
        return scheduler;
    }

    /**
     * New a ThreadPoolScheduler
     *
     * @param plugin the plugin
     * @param poolSize the thread pool size
     * @return a ThreadPoolScheduler
     */
    public static Scheduler newThreadPoolScheduler(@NotNull Plugin plugin,int poolSize) {
        Scheduler scheduler = new ThreadPoolScheduler(plugin,poolSize);
        PLUGIN_SCHEDULER_MAP.compute(plugin,(k,v) ->{
            if (v == null)
                v = Lists.newArrayList();
            v.add(scheduler);
            return v;
        });
        return scheduler;
    }

    /**
     * Close all the schedulers belonging to the plugin
     *
     * @param plugin the plugin
     */
    public static void close(Plugin plugin) {
        for (Scheduler scheduler : PLUGIN_SCHEDULER_MAP.getOrDefault(plugin,Lists.newArrayList()))
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
        for (Plugin plugin : PLUGIN_SCHEDULER_MAP.keySet()) {
            if (plugin != Main.getMainPlugin())
                ret = true;
            close(plugin);
        }
        return ret;
    }
}
