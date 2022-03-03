package top.focess.qq.core.debug;

import com.google.common.collect.Maps;
import top.focess.qq.FocessQQ;
import top.focess.qq.api.exceptions.SectionStartException;
import top.focess.qq.api.schedule.Scheduler;
import top.focess.qq.api.schedule.Schedulers;
import top.focess.qq.api.schedule.Task;

import java.time.Duration;
import java.util.Map;

public class Section {

    private static final Scheduler SCHEDULER = Schedulers.newFocessScheduler(FocessQQ.getMainPlugin());

    private static final Map<String, Task> TASK_MAP = Maps.newHashMap();

    public static void startSection(String name, Task task, Duration timeout) {
        if (TASK_MAP.containsKey(name))
            throw new SectionStartException(name);
        TASK_MAP.put(name,SCHEDULER.run(()->{
            task.cancel(true);
        },timeout));
    }

    public static void startSection(String name, Task task) {
        startSection(name,task,Duration.ofMinutes(10));
    }

    public static void stopSection(String name){
        TASK_MAP.remove(name);
    }
}
