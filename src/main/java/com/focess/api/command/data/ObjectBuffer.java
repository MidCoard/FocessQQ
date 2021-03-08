package com.focess.api.command.data;

import java.nio.BufferOverflowException;

public class ObjectBuffer extends DataBuffer<Object> {


    private final Object[] objects;

    private int pos;

    private int limit;

    public ObjectBuffer(int size) {
        this.objects = new Object[size];
        this.pos = 0;
        this.limit = size;
    }

    public static ObjectBuffer allocate(int size) {
        return new ObjectBuffer(size);
    }

    @Override
    public void flip() {
        this.limit = pos;
        this.pos = 0;
    }

    @Override
    public void put(Object o) {
        check();
        objects[pos++] = o;
    }

    private void check() {
        if (pos == limit)
            throw new BufferOverflowException();
    }

    @Override
    public Object get() {
        check2();
        return objects[pos++];
    }

    private void check2() {
        if (pos == limit)
            throw new IndexOutOfBoundsException(pos + ":" + limit);
    }
}
