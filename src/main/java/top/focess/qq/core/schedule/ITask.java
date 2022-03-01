package top.focess.qq.core.schedule;

import top.focess.qq.api.schedule.Task;

import java.time.Duration;

interface ITask extends Task {

    void run();

    Duration getPeriod();

    void setNativeTask(ComparableTask task);
}
