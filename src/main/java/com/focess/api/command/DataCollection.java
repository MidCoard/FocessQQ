package com.focess.api.command;

import com.focess.api.Plugin;
import com.focess.api.command.data.StringBuffer;
import com.focess.api.command.data.*;
import com.google.common.collect.Maps;

import java.nio.*;
import java.util.Map;
import java.util.UUID;

public class DataCollection {

    private static final Map<Class<?>, BufferGetter> registeredBuffers = Maps.newHashMap();
    private final IntBuffer intBuffer;
    private final DoubleBuffer doubleBuffer;
    private final FloatBuffer floatBuffer;
    private final BooleanBuffer booleanBuffer;
    private final ByteBuffer byteBuffer;
    private final LongBuffer longBuffer;
    private final CharBuffer charBuffer;
    private final ShortBuffer shortBuffer;
    private final StringBuffer stringBuffer;
    private final UUIDBuffer _UUIDBuffer;
    private final ObjectBuffer objectBuffer;
    private final StringBuffer defaultBuffer;
    private final PluginBuffer pluginBuffer;
    private final Map<Class<?>, DataBuffer> buffers = Maps.newHashMap();

    public DataCollection(int size) {
        this.defaultBuffer = StringBuffer.allocate(size);
        this.intBuffer = IntBuffer.allocate(size);
        this.doubleBuffer = DoubleBuffer.allocate(size);
        this.floatBuffer = FloatBuffer.allocate(size);
        this.booleanBuffer = BooleanBuffer.allocate(size);
        this.byteBuffer = ByteBuffer.allocate(size);
        this.longBuffer = LongBuffer.allocate(size);
        this.charBuffer = CharBuffer.allocate(size);
        this.shortBuffer = ShortBuffer.allocate(size);
        this.stringBuffer = StringBuffer.allocate(size);
        this._UUIDBuffer = UUIDBuffer.allocate(size);
        this.objectBuffer = ObjectBuffer.allocate(size);
        this.pluginBuffer = PluginBuffer.allocate(size);
        for (Class<?> c : registeredBuffers.keySet())
            buffers.put(c, registeredBuffers.get(c).newBuffer(size));
    }

    public static void registeredBuffer(Class<?> c, BufferGetter bufferGetter) {
        registeredBuffers.put(c, bufferGetter);
    }

    public void flip() {
        this.defaultBuffer.flip();
        this.intBuffer.flip();
        this.doubleBuffer.flip();
        this.floatBuffer.flip();
        this.booleanBuffer.flip();
        this.byteBuffer.flip();
        this.longBuffer.flip();
        this.charBuffer.flip();
        this.shortBuffer.flip();
        this.stringBuffer.flip();
        this._UUIDBuffer.flip();
        this.objectBuffer.flip();
        this.pluginBuffer.flip();
        for (Class<?> c : buffers.keySet())
            buffers.get(c).flip();
    }

    void write(String s) {
        defaultBuffer.put(s);
    }

    void writeInt(int i) {
        intBuffer.put(i);
    }

    void writeDouble(double d) {
        doubleBuffer.put(d);
    }

    void writeFloat(float f) {
        floatBuffer.put(f);
    }

    void writeBoolean(boolean b) {
        booleanBuffer.put(b);
    }

    void writeByte(byte b) {
        byteBuffer.put(b);
    }

    void writeLong(long l) {
        longBuffer.put(l);
    }

    void writeChar(char c) {
        charBuffer.put(c);
    }

    void writeShort(short s) {
        shortBuffer.put(s);
    }

    void writeString(String s) {
        stringBuffer.put(s);
    }

    void writeUUID(UUID u) {
        _UUIDBuffer.put(u);
    }

    void writeObject(Object o) {
        objectBuffer.put(o);
    }

    public String get() {
        return defaultBuffer.get();
    }

    public int getInt() {
        return intBuffer.get();
    }

    public double getDouble() {
        return doubleBuffer.get();
    }

    public float getFloat() {
        return floatBuffer.get();
    }

    public boolean getBoolean() {
        return booleanBuffer.get();
    }

    public byte getByte() {
        return byteBuffer.get();
    }

    public long getLong() {
        return longBuffer.get();
    }

    public char getChar() {
        return charBuffer.get();
    }

    public short getShort() {
        return shortBuffer.get();
    }

    public String getString() {
        return stringBuffer.get();
    }

    public UUID getUUID() {
        return _UUIDBuffer.get();
    }

    public Object getObject() {
        return objectBuffer.get();
    }

    public void writePlugin(Plugin p) {
        this.pluginBuffer.put(p);
    }

    public Plugin getPlugin() {
        return this.pluginBuffer.get();
    }

    public <T> void writeT(Class<T> cls, T t) {
        buffers.compute(cls, (Key, value) -> {
            if (value == null)
                throw new UnsupportedOperationException();
            value.put(t);
            return value;
        });
    }

    public <T> T getT(Class<T> c) {
        return (T) buffers.get(c).get();
    }

    public interface BufferGetter {
        DataBuffer<?> newBuffer(int size);
    }
}
