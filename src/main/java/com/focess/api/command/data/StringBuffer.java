package com.focess.api.command.data;

import java.nio.CharBuffer;
import java.nio.IntBuffer;

/**
 * Represent a buffer of String.
 */
public class StringBuffer extends DataBuffer<String> {

    private final IntBuffer intBuffer;

    private final CharBuffer[] charBuffers;
    private int pos = 0;

    private StringBuffer(int size) {
        this.intBuffer = IntBuffer.allocate(size);
        this.charBuffers = new CharBuffer[size];
    }

    /**
     * Allocate a StringBuffer with fixed size
     *
     * @param size the target buffer size
     * @return a StringBuffer with fixed size
     */
    public static StringBuffer allocate(int size) {
        return new StringBuffer(size);
    }

    public void flip() {
        this.intBuffer.flip();
    }

    public void put(String s) {
        charBuffers[pos] = CharBuffer.allocate(s.length()).put(s);
        charBuffers[pos].flip();
        intBuffer.put(pos++);
    }

    @Override
    public String get() {
        return new String(charBuffers[intBuffer.get()].array());
    }
}
