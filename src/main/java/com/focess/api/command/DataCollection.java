package com.focess.api.command;

import com.focess.Main;
import com.focess.api.command.data.DataBuffer;
import com.focess.api.plugin.Plugin;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import java.util.List;
import java.util.Map;

/**
 * Store and parser arguments for better CommandExecutor usage.
 */
public class DataCollection {

    private static final Map<Plugin,List<DataConverter<?>>> PLUGIN_DATA_CONVERTER_MAP = Maps.newConcurrentMap();
    private static final Map<DataConverter<?>,BufferGetter> DATA_CONVERTER_BUFFER_MAP = Maps.newConcurrentMap();
    private final Map<Class<?>, DataBuffer> buffers = Maps.newHashMap();

    /**
     * Initialize the DataCollection with fixed size.
     *
     * @param dataConverters the data converters
     */
    public DataCollection(DataConverter<?>[] dataConverters) {
        Map<DataConverter<?>,Integer> map = Maps.newHashMap();
        for (DataConverter<?> dataConverter : dataConverters)
            map.compute(dataConverter, (k, v) -> {
              if (v == null)
                  v = 0;
              v++;
              return v;
            });
        for (DataConverter<?> dataConverter : map.keySet())
            buffers.put(dataConverter.getTargetClass(), DATA_CONVERTER_BUFFER_MAP.get(dataConverter).newBuffer(map.get(dataConverter)));
    }

    /**
     * Register the getter of the buffer
     *
     * @param plugin the plugin
     * @param dataConverter the buffer data converter
     * @param bufferGetter the getter of the buffer
     */
    public static void register(Plugin plugin, DataConverter<?> dataConverter,BufferGetter bufferGetter) {
        PLUGIN_DATA_CONVERTER_MAP.compute(plugin, (k, v) -> {
           if (v == null)
               v = Lists.newArrayList();
           v.add(dataConverter);
           return v;
        });
        DATA_CONVERTER_BUFFER_MAP.put(dataConverter,bufferGetter);
    }

    /**
     * Unregister the getter of the buffers by plugin
     * @param plugin the plugin
     */
    public static void unregister(Plugin plugin) {
        for (DataConverter<?> dataConverter : PLUGIN_DATA_CONVERTER_MAP.getOrDefault(plugin, Lists.newArrayList()))
            DATA_CONVERTER_BUFFER_MAP.remove(dataConverter);
        PLUGIN_DATA_CONVERTER_MAP.remove(plugin);
    }

    /**
     * Unregister all the getter of the buffers
     */
    public static boolean unregisterAll() {
        boolean ret = false;
        for (Plugin plugin : PLUGIN_DATA_CONVERTER_MAP.keySet()) {
            if (plugin != Main.getMainPlugin())
                ret = true;
            unregister(plugin);
        }
        PLUGIN_DATA_CONVERTER_MAP.clear();
        return ret;
    }


    /**
     * Flip all the buffers. Make them all readable.
     */
    void flip() {
        for (Class<?> c : buffers.keySet())
            buffers.get(c).flip();
    }

    /**
     * Write a String argument
     *
     * @param s String argument
     */
    void write(String s) {
        this.write(String.class, s);
    }

    /**
     * Write a int argument
     *
     * @param i int argument
     */
    void writeInt(int i) {
        this.write(Integer.class, i);
    }

    /**
     * Write a double argument
     *
     * @param d double argument
     */
    void writeDouble(double d) {
        this.write(Double.class, d);
    }

    /**
     * Write a boolean argument
     *
     * @param b boolean argument
     */
    void writeBoolean(boolean b) {
        this.write(Boolean.class, b);
    }

    /**
     * Write a long argument
     *
     * @param l long argument
     */
    void writeLong(long l) {
        this.write(Long.class, l);
    }

    /**
     * Get String argument in order
     *
     * @return the String argument in order
     */
    public String get() {
        return this.get(String.class);
    }

    /**
     * Get int argument in order
     *
     * @return the int argument in order
     */
    public int getInt() {
        return this.get(Integer.class);
    }

    /**
     * Get double argument in order
     *
     * @return the double argument in order
     */
    public double getDouble() {
        return this.get(Double.class);
    }

    /**
     * Get boolean argument in order
     *
     * @return the boolean argument in order
     */
    public boolean getBoolean() {
        return this.get(Boolean.class);
    }

    /**
     * Get long argument in order
     *
     * @return the long argument in order
     */
    public long getLong() {
        return this.get(Long.class);
    }

    /**
     * Write a Plugin argument
     *
     * @param p Plugin argument
     */
    public void writePlugin(Plugin p) {
        this.write(Plugin.class, p);
    }

    /**
     * Get Plugin argument in order
     *
     * @return the Plugin argument in order
     */
    public Plugin getPlugin() {
        return this.get(Plugin.class);
    }

    /**
     * Get Command argument in order
     * @return the Command argument in order
     */
    public Command getCommand() {
        return this.get(Command.class);
    }

    /**
     * Write a Command argument
     * @param command Command argument
     */
    public void writeCommand(Command command) {
        this.write(Command.class, command);
    }

    /**
     * Write customize buffer element
     *
     * @param cls the buffer elements' class
     * @param t the buffer element
     * @param <T> the buffer elements' type
     * @throws UnsupportedOperationException if the buffer is not registered
     */
    public <T> void write(Class<T> cls, T t) {
        buffers.compute(cls, (Key, value) -> {
            if (value == null)
                throw new UnsupportedOperationException();
            value.put(t);
            return value;
        });
    }

    /**
     * Get customize buffer element
     *
     * @param c the buffer elements' class
     * @param <T> the buffer elements' type
     * @throws UnsupportedOperationException if the buffer is not registered
     * @return T the buffer element
     */
    public <T> T get(Class<T> c) {
        if (buffers.get(c) == null)
            throw new UnsupportedOperationException();
        return (T) buffers.get(c).get();
    }

    /**
     * Represents a getter for buffer.
     *
     * This is a functional interface whose functional method is {@link BufferGetter#newBuffer(int)}.
     */
    @FunctionalInterface
    public interface BufferGetter {
        /**
         * Instance a buffer with fixed size
         *
         * @param size the initialized size of the buffer
         * @return the buffer
         */
        DataBuffer<?> newBuffer(int size);
    }
}
