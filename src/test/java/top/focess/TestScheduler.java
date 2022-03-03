package top.focess;

public class TestScheduler {

    public static void main(String[] args) throws Exception{
//        Callable<String> callable = () -> {
//            System.out.println(1);
//            return new NetworkHandler(FocessQQ.getMainPlugin()).request("https://www.baidu.com", NetworkHandler.RequestType.GET).getResponse();
//        };
//        Scheduler scheduler = Schedulers.newThreadPoolScheduler(new FocessQQ.MainPlugin(),3);
//        Callback<String> callback = scheduler.submit(callable);
//        System.out.println(callback.waitCall());
//        scheduler.run(()->{
//            System.out.println("sb2" + System.currentTimeMillis());
//        }, Duration.ofSeconds(5));
//
//        scheduler.run(()->{
//            System.out.println("sb1" + System.currentTimeMillis());
//        }, Duration.ofSeconds(1));
//
//        Task t = scheduler.run(()->{
//            System.out.println("sb3" + System.currentTimeMillis());
//        }, Duration.ofSeconds(6));
//
//        scheduler.runTimer(()->{
//            System.out.println("sb4" + System.currentTimeMillis());
//            t.cancel();
//        }, Duration.ofSeconds(2),Duration.ofSeconds(1));
        Thread thread = new Thread(){
            @Override
            public void run() {
                while(true) {
                    System.out.println(1);
                }
            }
        };
        thread.start();
        Thread.sleep(1000);
        thread.stop();
        thread.start();

    }
}
