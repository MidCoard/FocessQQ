package top.focess.qq.api.command.data;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.nio.CharBuffer;

/**
 * Represent a buffer of String.
 */
public class StringBuffer extends DataBuffer<String> {

    private final IntBuffer intBuffer;

    private final CharBuffer[] charBuffers;
    private int pos;

    private StringBuffer(final int size) {
        this.intBuffer = IntBuffer.allocate(size);
        this.charBuffers = new CharBuffer[size];
    }

    /**
     * Allocate a StringBuffer with fixed size
     *
     * @param size the target buffer size
     * @return a StringBuffer with fixed size
     */
    @NotNull
    @Contract("_ -> new")
    public static StringBuffer allocate(final int size) {
        return new StringBuffer(size);
    }

    public void flip() {
        this.intBuffer.flip();
    }

    public void put(@NotNull final String s) {
        this.charBuffers[this.pos] = CharBuffer.allocate(s.length()).put(s);
        this.charBuffers[this.pos].flip();
        this.intBuffer.put(this.pos++);
    }

    @NotNull
    @Override
    public String get() {
        return new String(this.charBuffers[this.intBuffer.get()].array());
    }

    @NotNull
    @Override
    public String get(final int index) {
        return new String(this.charBuffers[this.intBuffer.get(index)].array());
    }
}
