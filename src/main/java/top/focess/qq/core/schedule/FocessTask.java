package top.focess.qq.core.schedule;

import com.google.common.collect.Lists;
import top.focess.qq.api.plugin.Plugin;
import top.focess.qq.api.schedule.Scheduler;
import top.focess.qq.api.schedule.Task;

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.time.Duration;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

public class FocessTask implements Task, ITask {

    //for debug
    private static final List<WeakReference<FocessTask>> TEST_TASKS = Lists.newArrayList();

    private final Runnable runnable;
    private final Scheduler scheduler;
    private final String name;
    private Duration period;
    protected boolean isRunning = false;
    private boolean isPeriod = false;
    protected boolean isFinished = false;
    private ComparableTask nativeTask;
    protected ExecutionException exception;

    //for debug
    public static List<FocessTask> getFinishedTasks() {
        return TEST_TASKS.stream().filter(weakReference -> weakReference.get() != null && weakReference.get().isFinished).map(WeakReference::get).collect(Collectors.toList());
    }
    //for debug
    public static List<FocessTask> getCancelledTasks() {
        return TEST_TASKS.stream().filter(weak -> weak.get() != null).map(Reference::get).filter(FocessTask::isCancelled).collect(Collectors.toList());
    }
    //for debug
    public static List<FocessTask> getTasks() {
        return TEST_TASKS.stream().filter(weak -> weak.get() != null).map(Reference::get).filter(i->!i.isCancelled() && !i.isFinished()).collect(Collectors.toList());
    }

    FocessTask(Runnable runnable, Scheduler scheduler) {
        this.runnable = runnable;
        this.scheduler = scheduler;
        this.name = scheduler.getName() + "-" + UUID.randomUUID().toString().substring(0,8);
        TEST_TASKS.add(new WeakReference<>(this));
    }

    FocessTask(Runnable runnable, Duration period,Scheduler scheduler) {
        this(runnable,scheduler);
        this.isPeriod = true;
        this.period = period;
    }

    @Override
    public void setNativeTask(ComparableTask nativeTask) {
        this.nativeTask = nativeTask;
    }

    @Override
    public synchronized void clear() {
        this.isFinished = false;
        this.isRunning = false;
    }

    @Override
    public synchronized void startRun() {
        this.isRunning = true;
    }

    @Override
    public synchronized void endRun() {
        this.isRunning = false;
        this.isFinished = true;
        this.notifyAll();
    }

    @Override
    public void setException(ExecutionException e) {
        this.exception = e;
    }

    @Override
    public boolean isSingleThread() {
        return this.scheduler instanceof ThreadPoolScheduler;
    }

    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
        return this.nativeTask.cancel(mayInterruptIfRunning);
    }

    @Override
    public synchronized boolean isRunning() {
        return isRunning;
    }

    @Override
    public Plugin getPlugin() {
        return this.scheduler.getPlugin();
    }

    @Override
    public Scheduler getScheduler() {
        return this.scheduler;
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public boolean isPeriod() {
        return this.isPeriod;
    }

    @Override
    public synchronized boolean isFinished() {
        return !this.isPeriod && this.isFinished;
    }

    @Override
    public boolean isCancelled() {
        return this.nativeTask.isCancelled();
    }

    @Override
    public synchronized void join() throws InterruptedException, CancellationException, ExecutionException {
        if (this.isFinished())
            return;
        if (this.isCancelled())
            throw new CancellationException();
        this.wait();
        if (this.isCancelled())
            throw new CancellationException();
        if (this.exception != null)
            throw this.exception;
    }

    @Override
    public void run() throws ExecutionException {
        this.runnable.run();
    }

    @Override
    public Duration getPeriod() {
        return this.period;
    }
}
