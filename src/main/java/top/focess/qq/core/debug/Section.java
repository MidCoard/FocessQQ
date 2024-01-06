package top.focess.qq.core.debug;

import org.jetbrains.annotations.NotNull;
import top.focess.qq.FocessQQ;
import top.focess.qq.api.scheduler.Schedulers;
import top.focess.scheduler.Scheduler;
import top.focess.scheduler.Task;

import java.time.Duration;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicReference;

public class Section {

    private static final Scheduler SCHEDULER = Schedulers.newFocessScheduler(FocessQQ.getMainPlugin(), "Section");

    private final String name;
    private final Task task;

    private Section(final String name, final Task task) {
        this.name = name;
        this.task = task;
    }

    @NotNull
    public static Section startSection(final String name, final Future<?> task, final Duration timeout) {
        final Task t = SCHEDULER.run(() -> {
            task.cancel(true);
            FocessQQ.getLogger().debugLang("debug-section-timeout", name);
        }, timeout, name);
        return new Section(name, t);
    }

    @NotNull
    public static Section startSection(final String name, final Task task, final Duration timeout) {
        System.out.println(System.currentTimeMillis());
        AtomicReference<Task> taskAtomicReference = new AtomicReference<>();
        final Task t = SCHEDULER.run(() -> {
            Task task1 = taskAtomicReference.get();
            System.out.println(task1.getName());
            System.out.println(System.currentTimeMillis());
            task.cancel(true);
            FocessQQ.getLogger().debugLang("debug-section-timeout", name);
        }, timeout, name);
        taskAtomicReference.set(t);
        return new Section(name, t);
    }

    public void stop() {
        this.task.cancel();
    }

    public String getName() {
        return this.name;
    }
}
