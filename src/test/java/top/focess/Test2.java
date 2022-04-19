package top.focess;

import top.focess.command.data.StringBuffer;
import top.focess.qq.FocessQQ;
import top.focess.qq.api.schedule.Schedulers;
import top.focess.scheduler.Scheduler;

public class Test2 {
    public static void main(String[] args) {
        Scheduler scheduler = Schedulers.newFocessScheduler(new FocessQQ.MainPlugin(), "test");
        scheduler.run(()->{
            StringBuffer sb = StringBuffer.allocate(20);
            sb.put("hello");
            sb.put("world");
            sb.put("focess");
            sb.flip();
            System.out.println(sb.get());
        });

    }
}
