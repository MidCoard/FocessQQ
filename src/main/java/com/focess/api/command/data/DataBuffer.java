package com.focess.api.command.data;

public abstract class DataBuffer<T> {
    public abstract void flip();

    public abstract void put(T t);

    public abstract T get();
}
