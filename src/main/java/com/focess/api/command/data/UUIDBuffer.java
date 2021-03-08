package com.focess.api.command.data;

import java.util.UUID;

public class UUIDBuffer extends DataBuffer<UUID> {
    private final StringBuffer stringBuffer;

    public UUIDBuffer(int size) {
        this.stringBuffer = StringBuffer.allocate(size);
    }

    public static UUIDBuffer allocate(int size) {
        return new UUIDBuffer(size);
    }

    public void flip() {
        this.stringBuffer.flip();
    }

    public void put(UUID u) {
        stringBuffer.put(u.toString());
    }

    @Override
    public UUID get() {
        return UUID.fromString(stringBuffer.get());
    }
}
