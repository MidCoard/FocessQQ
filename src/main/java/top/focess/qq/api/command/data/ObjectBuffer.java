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

    private ObjectBuffer(int size) {
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
        if (pos == limit)
            throw new BufferOverflowException();
        objects[pos++] = o;
    }

    @Override
    public Object get() {
        if (pos == limit)
            throw new BufferUnderflowException();
        return objects[pos++];
    }

    @Override
    public Object get(int index) {
        return objects[index];
    }
}
