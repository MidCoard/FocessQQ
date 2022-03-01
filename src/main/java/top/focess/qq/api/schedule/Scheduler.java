package top.focess.qq.api.schedule;

import top.focess.qq.api.plugin.Plugin;

import java.time.Duration;
import java.util.concurrent.Callable;

public interface Scheduler {

    default Task run(Runnable runnable) {
        return this.run(runnable, Duration.ZERO);
    }

    Task run(Runnable runnable, Duration delay);

    Task runTimer(Runnable runnable, Duration delay, Duration period);

    default  <V> Callback<V> submit(Callable<V> callable) {
        return this.submit(callable, Duration.ZERO);
    }

    <V> Callback<V> submit(Callable<V> callable, Duration delay);

    void cancelAll();

    String getName();

    Plugin getPlugin();

    void close();
}
