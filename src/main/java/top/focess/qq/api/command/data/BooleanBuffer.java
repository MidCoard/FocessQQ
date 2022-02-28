package top.focess.qq.api.command.data;

import java.nio.ByteBuffer;

/**
 * Represent a buffer of Boolean.
 */
public class BooleanBuffer extends DataBuffer<Boolean> {

    private final ByteBuffer byteBuffer;

    private BooleanBuffer(int size) {
        this.byteBuffer = ByteBuffer.allocate(size);
    }

    /**
     * Allocate a BooleanBuffer with fixed size
     *
     * @param size the target buffer size
     * @return a BooleanBuffer with fixed size
     */
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
