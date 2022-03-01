package top.focess.qq.api.schedule;

import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

public interface Callback<V> extends Task, Future<V> {

    V call();

    default V waitCall() {
        while (!isFinished());
        return call();
    }

    @Override
    default boolean isDone() {
        return this.isFinished();
    }

    @Override
    default V get() {
        return this.waitCall();
    }

    @Override
    default V get(long timeout, TimeUnit unit){
        throw new UnsupportedOperationException();
    }
}
