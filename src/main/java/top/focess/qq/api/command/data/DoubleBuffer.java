package top.focess.qq.api.command.data;

/**
 * Represent a buffer of Double.
 */
public class DoubleBuffer extends DataBuffer<Double> {

    private final java.nio.DoubleBuffer buffer;

    private DoubleBuffer(final int size) {
        this.buffer = java.nio.DoubleBuffer.allocate(size);
    }

    /**
     * Allocate a DoubleBuffer with fixed size
     *
     * @param size the target buffer size
     * @return a DoubleBuffer with fixed size
     */
    public static DoubleBuffer allocate(final int size) {
        return new DoubleBuffer(size);
    }

    @Override
    public void flip() {
        this.buffer.flip();
    }

    @Override
    public void put(final Double d) {
        this.buffer.put(d);
    }

    @Override
    public Double get() {
        return this.buffer.get();
    }

    @Override
    public Double get(final int index) {
        return this.buffer.get(index);
    }
}
