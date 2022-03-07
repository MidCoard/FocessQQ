package top.focess.qq.core.debug;

import top.focess.qq.FocessQQ;
import top.focess.qq.api.schedule.Scheduler;
import top.focess.qq.api.schedule.Schedulers;
import top.focess.qq.api.schedule.Task;

import java.time.Duration;
import java.util.concurrent.Future;

public class Section {

    private static final Scheduler SCHEDULER = Schedulers.newFocessScheduler(FocessQQ.getMainPlugin());

    private final String name;
    private final Task task;

    public static Section startSection(String name, Future<?> task, Duration timeout) {
        Task t = SCHEDULER.run(()->{
            task.cancel(true);
            FocessQQ.getLogger().debugLang("debug-section-timeout",name);
        },timeout);
        return new Section(name,t);
    }

    public static Section startSection(String name, Task task, Duration timeout) {
        Task t = SCHEDULER.run(()->{
            task.cancel(true);
            FocessQQ.getLogger().debugLang("debug-section-timeout",name);
        },timeout);
        return new Section(name,t);
    }

    private Section(String name, Task task) {
        this.name = name;
        this.task = task;
    }

    public void stop(){
        task.cancel();
    }

    public String getName() {
        return name;
    }
}
