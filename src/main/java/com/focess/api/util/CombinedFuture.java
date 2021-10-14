package com.focess.api.util;

import com.google.common.collect.Lists;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Represent a Future of Boolean instance
 */
public class CombinedFuture implements Future<Boolean> {

    private final List<Future<Boolean>> futures = Lists.newArrayList();

    /**
     * Combine a Future of Boolean
     *
     * @param future the future need to be combined
     */
    public void combine(Future<Boolean> future) {
        futures.add(future);
    }

    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
        boolean ret = true;
        for (Future<Boolean> future:futures)
            if (!future.cancel(mayInterruptIfRunning))
                ret = false;
        return ret;
    }

    @Override
    public boolean isCancelled() {
        boolean ret = true;
        for (Future<Boolean> future:futures)
            if (!future.isCancelled())
                ret = false;
        return ret;
    }

    @Override
    public boolean isDone() {
        boolean ret = true;
        for (Future<Boolean> future:futures)
            if (!future.isDone())
                ret = false;
        return ret;
    }

    @Override
    public Boolean get() throws InterruptedException, ExecutionException {
       boolean ret = true;
       for (Future<Boolean> future:futures)
           if (!future.get())
               ret = false;
       return ret;
    }

    @Override
    public Boolean get(long timeout, @NotNull TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
        boolean ret = true;
        for (Future<Boolean> future:futures)
            if (!future.get(timeout,unit))
                ret = false;
        return ret;
    }
}
