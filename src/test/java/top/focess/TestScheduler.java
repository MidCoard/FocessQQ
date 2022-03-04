package top.focess;

import top.focess.qq.FocessQQ;
import top.focess.qq.api.schedule.Callback;
import top.focess.qq.api.schedule.Scheduler;
import top.focess.qq.api.schedule.Schedulers;
import top.focess.qq.api.util.network.NetworkHandler;

import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

public class TestScheduler {

    public static void main(String[] args) throws Exception{
        Callable<String> callable = () -> {
//            Thread.sleep(3000);
            return new NetworkHandler(FocessQQ.getMainPlugin()).request("https://www.baidu.com", NetworkHandler.RequestType.GET).getResponse();
        };
        Scheduler scheduler = Schedulers.newThreadPoolScheduler(new FocessQQ.MainPlugin(),3);
        Callback<String> callback = scheduler.submit(callable);
        scheduler.run(()->{
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.println(callback.cancel(true));
        });
        System.out.println(System.currentTimeMillis());
        System.out.println(callback.get(5000, TimeUnit.MILLISECONDS));
        System.out.println(System.currentTimeMillis());
    }
}
