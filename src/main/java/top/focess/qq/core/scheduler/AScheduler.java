package top.focess.qq.core.scheduler;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.jetbrains.annotations.Nullable;
import top.focess.qq.FocessQQ;
import top.focess.qq.api.plugin.Plugin;
import top.focess.scheduler.Callback;
import top.focess.scheduler.CatchExceptionHandler;
import top.focess.scheduler.Scheduler;
import top.focess.scheduler.Task;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

public class AScheduler implements Scheduler {

    private static final Map<Plugin, List<Scheduler>> PLUGIN_SCHEDULER_MAP = Maps.newConcurrentMap();

    private final Plugin plugin;
    private final Scheduler scheduler;

    public AScheduler(Plugin plugin, Scheduler scheduler) {
        this.plugin = plugin;
        this.scheduler = scheduler;
        this.scheduler.setUncaughtExceptionHandler((t, e) -> {
            FocessQQ.getLogger().thrLang("exception-scheduler-uncaught", e, this.getName());
        });
        this.scheduler.setCatchExceptionHandler((t, e) -> {
            FocessQQ.getLogger().thrLang("exception-scheduler", e, this.getName());
        });
        PLUGIN_SCHEDULER_MAP.compute(plugin, (k, v) -> {
            if (v == null)
                v = Lists.newCopyOnWriteArrayList();
            v.add(this);
            return v;
        });
    }

    public Plugin getPlugin() {
        return plugin;
    }

    @Override
    public Task run(Runnable runnable, Duration delay) {
        return this.scheduler.run(runnable, delay);
    }

    @Override
    public Task runTimer(Runnable runnable, Duration delay, Duration period) {
        return this.scheduler.runTimer(runnable, delay, period);
    }

    @Override
    public <V> Callback<V> submit(Callable<V> callable, Duration delay) {
        return this.scheduler.submit(callable, delay);
    }

    @Override
    public void cancelAll() {
        this.scheduler.cancelAll();
    }

    @Override
    public String getName() {
        return this.scheduler.getName();
    }

    @Override
    public void close() {
        this.scheduler.close();
        PLUGIN_SCHEDULER_MAP.compute(this.plugin, (k, v) -> {
            if (v != null)
                v.remove(this);
            return v;
        });
    }

    @Override
    public boolean isClosed() {
        return this.scheduler.isClosed();
    }

    @Override
    public void closeNow() {
        this.scheduler.closeNow();
    }

    @Override
    public void setUncaughtExceptionHandler(Thread.UncaughtExceptionHandler uncaughtExceptionHandler) {
        this.scheduler.setUncaughtExceptionHandler(uncaughtExceptionHandler);
    }

    @Override
    @Nullable
    public Thread.UncaughtExceptionHandler getUncaughtExceptionHandler() {
        return this.scheduler.getUncaughtExceptionHandler();
    }

    @Override
    public @Nullable CatchExceptionHandler getCatchExceptionHandler() {
        return this.scheduler.getCatchExceptionHandler();
    }

    @Override
    public void setCatchExceptionHandler(CatchExceptionHandler catchExceptionHandler) {
        this.scheduler.setCatchExceptionHandler(catchExceptionHandler);
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

}
