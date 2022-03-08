package top.focess;

import top.focess.qq.FocessQQ;
import top.focess.qq.api.schedule.Scheduler;
import top.focess.qq.api.schedule.Schedulers;
import top.focess.qq.api.schedule.Task;

import java.util.concurrent.ExecutionException;

public class TestScheduler {

    public static void main(String[] args) throws ExecutionException, InterruptedException {
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        };
        Scheduler scheduler = Schedulers.newFocessScheduler(new FocessQQ.MainPlugin());
        Task task = scheduler.run(runnable);
        System.out.println(System.currentTimeMillis());
        try {
            task.join();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        task.join();
        System.out.println(System.currentTimeMillis());
    }
}
