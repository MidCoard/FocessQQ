package top.focess.qq.core.scheduler;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnmodifiableView;
import top.focess.qq.FocessQQ;
import top.focess.qq.api.plugin.Plugin;
import top.focess.qq.core.permission.Permission;
import top.focess.qq.core.permission.PermissionEnv;
import top.focess.scheduler.Callback;
import top.focess.scheduler.Scheduler;
import top.focess.scheduler.Task;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;
import java.util.function.Function;

@PermissionEnv(values = Permission.REMOVE_SCHEDULER)
public class AScheduler implements Scheduler {

    private static final Map<Plugin, List<Scheduler>> PLUGIN_SCHEDULER_MAP = Maps.newConcurrentMap();

    private final Plugin plugin;
    private final Scheduler scheduler;

    public AScheduler(final Plugin plugin, final Scheduler scheduler) {
        this.plugin = plugin;
        this.scheduler = scheduler;
        this.scheduler.setUncaughtExceptionHandler((t, e) -> FocessQQ.getLogger().thrLang("exception-scheduler-uncaught", e, this.getName()));
        PLUGIN_SCHEDULER_MAP.compute(plugin, (k, v) -> {
            if (v == null)
                v = Lists.newCopyOnWriteArrayList();
            v.add(this);
            return v;
        });
    }

    public Plugin getPlugin() {
        return this.plugin;
    }

    @Override
    public Task run(final Runnable runnable, final Duration delay) {
        return this.scheduler.run(runnable, delay);
    }

    @Override
    public Task run(final Runnable runnable, final Duration delay, final String name) {
        return this.scheduler.run(runnable, delay, name);
    }

    @Override
    public Task runTimer(final Runnable runnable, final Duration delay, final Duration period) {
        return this.scheduler.runTimer(runnable, delay, period);
    }

    @Override
    public Task runTimer(final Runnable runnable, final Duration delay, final Duration period, final String name) {
        return this.scheduler.runTimer(runnable, delay, period, name);
    }

    @Override
    public Task runTimer(Runnable runnable, Duration duration, Duration duration1, String s, Consumer<ExecutionException> consumer) {
        return this.scheduler.runTimer(runnable, duration, duration1, s, consumer);
    }

    @Override
    public <V> Callback<V> submit(final Callable<V> callable, final Duration delay) {
        return this.scheduler.submit(callable, delay);
    }

    @Override
    public <V> Callback<V> submit(final Callable<V> callable, final Duration duration, final String name) {
        return this.scheduler.submit(callable, duration, name);
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
        Permission.checkPermission(Permission.REMOVE_SCHEDULER);
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
    public void setUncaughtExceptionHandler(final Thread.UncaughtExceptionHandler uncaughtExceptionHandler) {
        this.scheduler.setUncaughtExceptionHandler(uncaughtExceptionHandler);
    }

    @Override
    @Nullable
    public Thread.UncaughtExceptionHandler getUncaughtExceptionHandler() {
        return this.scheduler.getUncaughtExceptionHandler();
    }

    @Override
    public @UnmodifiableView List<Task> getRemainingTasks() {
        return this.scheduler.getRemainingTasks();
    }

    @Override
    public Task run(final Runnable runnable, final Duration duration, final String s, final Consumer<ExecutionException> consumer) {
        return this.scheduler.run(runnable, duration, s, consumer);
    }

    @Override
    public <V> Callback<V> submit(final Callable<V> callable, final Duration duration, final String s, final Function<ExecutionException, V> function) {
        return this.scheduler.submit(callable, duration, s, function);
    }

    /**
     * Close all the schedulers belonging to the plugin
     *
     * @param plugin the plugin
     */
    public static void close(final Plugin plugin) {
        Permission.checkPermission(Permission.REMOVE_SCHEDULER);
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
        Permission.checkPermission(Permission.REMOVE_SCHEDULER);
        boolean ret = false;
        for (final Plugin plugin : PLUGIN_SCHEDULER_MAP.keySet()) {
            if (plugin != FocessQQ.getMainPlugin())
                ret = true;
            close(plugin);
        }
        return ret;
    }

}
