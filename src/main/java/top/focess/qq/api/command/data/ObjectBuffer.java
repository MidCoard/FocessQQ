package top.focess.qq.api.command.data;

import java.nio.BufferOverflowException;
import java.nio.BufferUnderflowException;

/**
 * Represent a buffer of Object.
 */
public class ObjectBuffer extends DataBuffer<Object> {

    private final Object[] objects;

    private int pos;

    private int limit;

    private ObjectBuffer(final int size) {
        this.objects = new Object[size];
        this.pos = 0;
        this.limit = size;
    }

    /**
     * Allocate a ObjectBuffer with fixed size
     *
     * @param size the target buffer size
     * @return a ObjectBuffer with fixed size
     */
    public static ObjectBuffer allocate(final int size) {
        return new ObjectBuffer(size);
    }

    @Override
    public void flip() {
        this.limit = this.pos;
        this.pos = 0;
    }

    @Override
    public void put(final Object o) {
        if (this.pos == this.limit)
            throw new BufferOverflowException();
        this.objects[this.pos++] = o;
    }

    @Override
    public Object get() {
        if (this.pos == this.limit)
            throw new BufferUnderflowException();
        return this.objects[this.pos++];
    }

    @Override
    public Object get(final int index) {
        return this.objects[index];
    }
}
