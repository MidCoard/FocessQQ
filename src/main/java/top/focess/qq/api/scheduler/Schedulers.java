package top.focess.qq.api.scheduler;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import top.focess.qq.FocessQQ;
import top.focess.qq.api.plugin.Plugin;
import top.focess.qq.core.permission.Permission;
import top.focess.qq.core.permission.PermissionEnv;
import top.focess.qq.core.scheduler.AScheduler;
import top.focess.scheduler.FocessScheduler;
import top.focess.scheduler.Scheduler;
import top.focess.scheduler.ThreadPoolScheduler;

/**
 * Used to create Scheduler. The scheduler factory.
 */
@PermissionEnv(values = {Permission.SCHEDULER, Permission.REMOVE_SCHEDULER})
public class Schedulers {

    private Schedulers() {
    }

    /**
     * New a FocessScheduler, the scheduler will run all tasks in time order.
     * For example, if the finish-time of the last task is after the start-time of the next task, the next task will only be executed after the last task is finished.
     * As a result, the task running in this scheduler cannot be cancelled if it is already running.
     *
     * @param plugin the plugin
     * @return a FocessScheduler
     * @see Schedulers#newFocessScheduler(Plugin)
     */
    @NotNull
    @Contract("_ -> new")
    public static Scheduler newFocessScheduler(@NotNull final Plugin plugin) {
        Permission.checkPermission(Permission.SCHEDULER);
        return new AScheduler(plugin,FocessScheduler.newPrefixFocessScheduler(plugin.getName()));
    }

    /**
     * New a FocessScheduler, the scheduler will run all tasks in time order.
     * For example, if the finish-time of the last task is after the start-time of the next task, the next task will only be executed after the last task is finished.
     * As a result, the task running in this scheduler cannot be cancelled if it is already running.
     *
     * @param plugin the plugin
     * @param name   the scheduler name
     * @return a FocessScheduler
     * @see Schedulers#newFocessScheduler(Plugin)
     */
    @NotNull
    @Contract("_, _ -> new")
    public static Scheduler newFocessScheduler(@NotNull final Plugin plugin, @NotNull final String name) {
        Permission.checkPermission(Permission.SCHEDULER);
        return new AScheduler(plugin, new FocessScheduler(name));
    }

    /**
     * New a ThreadPoolScheduler, the scheduler can run tasks in parallel.
     * The next task will be executed when there is an available thread.
     * As a result, the task running in this scheduler can be cancelled if it is already running.
     *
     * @param plugin   the plugin
     * @param poolSize the thread pool size
     * @return a ThreadPoolScheduler
     * @see Schedulers#newThreadPoolScheduler(Plugin, int, boolean, String)
     */
    @NotNull
    @Contract("_, _ -> new")
    public static Scheduler newThreadPoolScheduler(@NotNull final Plugin plugin, final int poolSize) {
        Permission.checkPermission(Permission.SCHEDULER);
        final ThreadPoolScheduler scheduler = new ThreadPoolScheduler(plugin.getName(),poolSize);
        scheduler.setThreadUncaughtExceptionHandler((t, e) -> FocessQQ.getLogger().thrLang("exception-thread-pool-scheduler-thread-uncaught", e, t.getName()));
        return new AScheduler(plugin, scheduler);
    }

    /**
     * New a ThreadPoolScheduler, the scheduler can run tasks in parallel.
     * The next task will be executed immediately if the immediate is true, otherwise the next task will be executed when there is an available thread.
     * As a result, the task running in this scheduler can be cancelled if it is already running.
     *
     * @param plugin    the plugin
     * @param poolSize  the thread pool size
     * @param immediate true if the task should run immediately when there is not enough threads, false otherwise
     * @param name      the scheduler name
     * @return a ThreadPoolScheduler
     * @see Schedulers#newThreadPoolScheduler(Plugin, int)
     */
    @NotNull
    @Contract("_, _, _, _ -> new")
    public static Scheduler newThreadPoolScheduler(@NotNull final Plugin plugin, final int poolSize, final boolean immediate, @NotNull final String name) {
        Permission.checkPermission(Permission.SCHEDULER);
        final ThreadPoolScheduler scheduler = new ThreadPoolScheduler(poolSize, immediate, name);
        scheduler.setThreadUncaughtExceptionHandler((t, e) -> FocessQQ.getLogger().thrLang("exception-thread-pool-scheduler-thread-uncaught", e, t.getName()));
        return new AScheduler(plugin,scheduler);
    }

    /**
     * Close all the schedulers belonging to the plugin
     *
     * @param plugin the plugin
     */
    public static void close(final Plugin plugin) {
        Permission.checkPermission(Permission.REMOVE_SCHEDULER);
        AScheduler.close(plugin);
    }

    /**
     * Close all the schedulers
     *
     * @return true if there are some schedulers not belonging to MainPlugin not been closed, false otherwise
     */
    public static boolean closeAll() {
        Permission.checkPermission(Permission.REMOVE_SCHEDULER);
        return AScheduler.closeAll();
    }

}
