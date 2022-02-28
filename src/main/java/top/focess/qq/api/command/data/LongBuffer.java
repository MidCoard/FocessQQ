package top.focess.qq.api.command.data;

/**
 * Represent a buffer of Long.
 */
public class LongBuffer extends DataBuffer<Long> {

    private final java.nio.LongBuffer buffer;

    private LongBuffer(int size) {
        this.buffer = java.nio.LongBuffer.allocate(size);
    }

    /**
     * Allocate a LongBuffer with fixed size
     *
     * @param size the target buffer size
     * @return a LongBuffer with fixed size
     */
    public static LongBuffer allocate(int size) {
        return new LongBuffer(size);
    }

    @Override
    public void flip() {
        this.buffer.flip();
    }

    @Override
    public void put(Long l) {
        this.buffer.put(l);
    }

    @Override
    public Long get() {
        return this.buffer.get();
    }
}
