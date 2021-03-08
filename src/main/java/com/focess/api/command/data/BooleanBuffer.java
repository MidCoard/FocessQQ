package com.focess.api.command.data;

import java.nio.ByteBuffer;

public class BooleanBuffer extends DataBuffer<Boolean> {

    private final ByteBuffer byteBuffer;

    public BooleanBuffer(int size) {
        this.byteBuffer = ByteBuffer.allocate(size);
    }
    public static BooleanBuffer allocate(int size) {
        return new BooleanBuffer(size);
    }
    @Override
    public void put(Boolean b) {
        byteBuffer.put((byte) (b ? 1 : 0));
    }

    @Override
    public Boolean get() {
        return byteBuffer.get() != 0;
    }

    @Override
    public void flip() {
        this.byteBuffer.flip();
    }
}
