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

public class Schedulers {

    private static final Map<Plugin, List<Scheduler>> PLUGIN_SCHEDULER_MAP = Maps.newHashMap();

    private Schedulers() {}

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

    public static void close(Plugin plugin) {
        for (Scheduler scheduler : PLUGIN_SCHEDULER_MAP.getOrDefault(plugin,Lists.newArrayList()))
            scheduler.close();
        PLUGIN_SCHEDULER_MAP.remove(plugin);
    }

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
