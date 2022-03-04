package top.focess;

import top.focess.qq.FocessQQ;
import top.focess.qq.api.schedule.Callback;
import top.focess.qq.api.schedule.Scheduler;
import top.focess.qq.api.schedule.Schedulers;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class TestScheduler {

    public static void main(String[] args){
        Callable<String> callable = () -> {
            Thread.sleep(3000);
            throw new NullPointerException();
//            return new NetworkHandler(FocessQQ.getMainPlugin()).request("https://www.baidu.com", NetworkHandler.RequestType.GET).getResponse();
        };
        Scheduler scheduler = Schedulers.newFocessScheduler(new FocessQQ.MainPlugin());
        Callback<String> callback = scheduler.submit(callable);
        System.out.println(System.currentTimeMillis());
        try {
            System.out.println(callback.get(5000, TimeUnit.MILLISECONDS));
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            e.printStackTrace();
        }
        System.out.println(System.currentTimeMillis());
    }
}
