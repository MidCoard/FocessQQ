package top.focess.qq.core.schedule;

import org.jetbrains.annotations.NotNull;
import top.focess.qq.FocessQQ;
import top.focess.qq.api.exceptions.TaskNotFinishedException;
import top.focess.qq.api.schedule.Callback;
import top.focess.qq.api.schedule.Scheduler;
import top.focess.qq.api.schedule.Schedulers;

import java.time.Duration;
import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.TimeUnit;

public class FocessCallback<V> extends FocessTask implements Callback<V> {

    private final static Scheduler DEFAULT_SCHEDULER = Schedulers.newThreadPoolScheduler(FocessQQ.getMainPlugin(),10);

    private final Callable<V> callback;
    private V value;

    FocessCallback(Callable<V> callback, Scheduler scheduler) {
        super(null,scheduler);
        this.callback = callback;
    }

    @Override
    public V call() {
        if (this.isCancelled())
            throw new CancellationException();
        if (!this.isFinished)
            throw new TaskNotFinishedException(this);
        return value;
    }

    @Override
    public V get(long timeout, @NotNull TimeUnit unit) {
        //todo
        DEFAULT_SCHEDULER.run(() -> {
            synchronized (FocessCallback.this) {
                FocessCallback.this.notify();
            }
        }, Duration.ofMillis(unit.toMillis(timeout)));
        synchronized (this) {
            try {
                this.wait();
            } catch (InterruptedException ignored) {
            }
        }
        return this.call();
    }

    @Override
    public void run() {
        this.isRunning = true;
        try {
            value = this.callback.call();
        } catch (Exception e) {
            value = null;
        }
        this.isRunning = false;
        this.isFinished = true;
        synchronized (this) {
            this.notify();
        }
    }

}
