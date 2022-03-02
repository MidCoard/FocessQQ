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
     * New a FocessScheduler
     *
     * @param plugin the plugin
     * @return a FocessScheduler
     */
    public static Scheduler newFocessScheduler(@NotNull Plugin plugin) {
        return new FocessScheduler(plugin);
    }

    /**
     * New a ThreadPoolScheduler
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
