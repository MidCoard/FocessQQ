package top.focess;

import top.focess.qq.FocessQQ;
import top.focess.qq.api.schedule.Callback;
import top.focess.qq.api.schedule.Scheduler;
import top.focess.qq.api.schedule.Schedulers;
import top.focess.qq.api.schedule.Task;
import top.focess.qq.api.util.network.NetworkHandler;

import java.time.Duration;
import java.util.concurrent.Callable;

public class TestScheduler {

    public static void main(String[] args) {
        Callable<String> callable = new Callable<String>() {
            @Override
            public String call() throws Exception {
                System.out.println(1);
                return new NetworkHandler().request("https://www.baidu.com", NetworkHandler.RequestType.GET).getResponse();
            }
        };
        Scheduler scheduler = Schedulers.newThreadPoolScheduler(new FocessQQ.MainPlugin(),3);
        Callback<String> callback = scheduler.submit(callable);
        System.out.println(callback.waitCall());
        scheduler.run(()->{
            System.out.println("sb2" + System.currentTimeMillis());
        }, Duration.ofSeconds(5));

        scheduler.run(()->{
            System.out.println("sb1" + System.currentTimeMillis());
        }, Duration.ofSeconds(1));

        Task t = scheduler.run(()->{
            System.out.println("sb3" + System.currentTimeMillis());
        }, Duration.ofSeconds(6));

        scheduler.runTimer(()->{
            System.out.println("sb4" + System.currentTimeMillis());
            t.cancel();
        }, Duration.ofSeconds(2),Duration.ofSeconds(1));


    }
}
