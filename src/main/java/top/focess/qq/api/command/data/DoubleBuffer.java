package top.focess.qq.api.command.data;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

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
    @NotNull
    @Contract("_ -> new")
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

    @NotNull
    @Override
    public Double get() {
        return this.buffer.get();
    }

    @NotNull
    @Override
    public Double get(final int index) {
        return this.buffer.get(index);
    }
}
