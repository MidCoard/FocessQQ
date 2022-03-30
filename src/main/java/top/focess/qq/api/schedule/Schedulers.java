package top.focess.qq.api.schedule;

import org.jetbrains.annotations.NotNull;
import top.focess.qq.api.plugin.Plugin;
import top.focess.qq.core.schedule.AScheduler;
import top.focess.qq.core.schedule.FocessScheduler;
import top.focess.qq.core.schedule.ThreadPoolScheduler;

/**
 * Used to create Scheduler. The scheduler factory.
 */
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
    public static Scheduler newFocessScheduler(@NotNull final Plugin plugin) {
        return new FocessScheduler(plugin);
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
    public static Scheduler newFocessScheduler(@NotNull final Plugin plugin, @NotNull final String name) {
        return new FocessScheduler(plugin, name);
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
    public static Scheduler newThreadPoolScheduler(@NotNull final Plugin plugin, final int poolSize) {
        return new ThreadPoolScheduler(plugin, poolSize);
    }

    /**
     * New a ThreadPoolScheduler, the scheduler can run tasks in parallel.
     * The next task will be executed immediately if the immediate is true, otherwise the next task will be executed when there is an available thread.
     * As a result, the task running in this scheduler can be cancelled if it is already running.
     *
     * @param plugin    the plugin
     * @param poolSize  the thread pool size
     * @param immediate true if the scheduler should run immediately, false otherwise
     * @param name      the scheduler name
     * @return a ThreadPoolScheduler
     * @see Schedulers#newThreadPoolScheduler(Plugin, int)
     */
    public static Scheduler newThreadPoolScheduler(@NotNull final Plugin plugin, final int poolSize, final boolean immediate, @NotNull final String name) {
        return new ThreadPoolScheduler(plugin, poolSize, immediate, name);
    }

    /**
     * Close all the schedulers belonging to the plugin
     *
     * @param plugin the plugin
     */
    public static void close(final Plugin plugin) {
        AScheduler.close(plugin);
    }

    /**
     * Close all the schedulers
     *
     * @return true if there are some schedulers not belonging to MainPlugin not been closed, false otherwise
     */
    public static boolean closeAll() {
        return AScheduler.closeAll();
    }

}
