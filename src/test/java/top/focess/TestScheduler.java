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

    public static void main(String[] args) throws Exception{
        Callable<String> callable = () -> {
            System.out.println(1);
            return new NetworkHandler(FocessQQ.getMainPlugin()).request("https://www.baidu.com", NetworkHandler.RequestType.GET).getResponse();
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
            while (true) {
                System.out.println("sb3" + System.currentTimeMillis());
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });

        scheduler.runTimer(()->{
            System.out.println("sb4" + System.currentTimeMillis());
            System.out.println(t.cancel(true));
        }, Duration.ofSeconds(2),Duration.ofSeconds(1));

    }
}
