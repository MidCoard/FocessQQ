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

    private Schedulers() {}

    /**
     * New a FocessScheduler, the scheduler will run all tasks in time order.
     * For example, if the finish-time of the last task is after the start-time of the next task, the next task will only be executed after the last task is finished.
     * As a result, the task running in this scheduler cannot be cancelled if it is already running.
     *
     * @param plugin the plugin
     * @return a FocessScheduler
     */
    public static Scheduler newFocessScheduler(@NotNull Plugin plugin) {
        return new FocessScheduler(plugin);
    }

    /**
     * New a ThreadPoolScheduler, the scheduler can run tasks in parallel.
     * So if the thread-pool is big enough, even if the finish-time of the last task is after the start-time of the next task, the next task will be executed immediately.
     * As a result, the task running in this scheduler can be cancelled if it is already running.
     *
     * @param plugin the plugin
     * @param poolSize the thread pool size
     * @return a ThreadPoolScheduler
     */
    public static Scheduler newThreadPoolScheduler(@NotNull Plugin plugin,int poolSize) {
        return new ThreadPoolScheduler(plugin,poolSize);
    }

    /**
     * Close all the schedulers belonging to the plugin
     *
     * @param plugin the plugin
     */
    public static void close(Plugin plugin) {
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
