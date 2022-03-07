package top.focess.qq.api.command.data;

/**
 * Represent a buffer of Int.
 */
public class IntBuffer extends DataBuffer<Integer> {

    private final java.nio.IntBuffer buffer;

    private IntBuffer(int size) {
        this.buffer = java.nio.IntBuffer.allocate(size);
    }

    /**
     * Allocate a IntBuffer with fixed size
     *
     * @param size the target buffer size
     * @return a IntBuffer with fixed size
     */
    public static IntBuffer allocate(int size) {
        return new IntBuffer(size);
    }

    @Override
    public void flip() {
        this.buffer.flip();
    }

    @Override
    public void put(Integer integer) {
        this.buffer.put(integer);
    }

    @Override
    public Integer get() {
        return this.buffer.get();
    }

    @Override
    public Integer get(int index) {
        return this.buffer.get(index);
    }
}
