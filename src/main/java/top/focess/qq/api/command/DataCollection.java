package top.focess.qq.api.command;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import top.focess.qq.FocessQQ;
import top.focess.qq.api.command.data.DataBuffer;
import top.focess.qq.api.plugin.Plugin;

import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Store and parser arguments for better CommandExecutor usage.
 */
public class DataCollection {

    private static final Map<Plugin, List<DataConverter<?>>> PLUGIN_DATA_CONVERTER_MAP = Maps.newConcurrentMap();
    private static final Map<DataConverter<?>, BufferGetter> DATA_CONVERTER_BUFFER_MAP = Maps.newConcurrentMap();
    private final Map<Class<?>, DataBuffer> buffers = Maps.newHashMap();

    /**
     * Initialize the DataCollection with fixed size.
     *
     * @param dataConverters the data converters
     */
    public DataCollection(@NotNull final DataConverter<?>[] dataConverters) {
        final Map<DataConverter<?>, Integer> map = Maps.newHashMap();
        for (final DataConverter<?> dataConverter : dataConverters)
            map.compute(dataConverter, (k, v) -> {
                if (v == null)
                    v = 0;
                v++;
                return v;
            });
        for (final DataConverter<?> dataConverter : map.keySet())
            this.buffers.put(dataConverter.getTargetClass(), DATA_CONVERTER_BUFFER_MAP.get(dataConverter).newBuffer(map.get(dataConverter)));
    }

    /**
     * Register the getter of the buffer
     *
     * @param plugin        the plugin
     * @param dataConverter the buffer data converter
     * @param bufferGetter  the getter of the buffer
     */
    public static void register(final Plugin plugin, final DataConverter<?> dataConverter, final BufferGetter bufferGetter) {
        PLUGIN_DATA_CONVERTER_MAP.compute(plugin, (k, v) -> {
            if (v == null)
                v = Lists.newArrayList();
            v.add(dataConverter);
            return v;
        });
        DATA_CONVERTER_BUFFER_MAP.put(dataConverter, bufferGetter);
    }

    /**
     * Unregister the getter of the buffers by plugin
     *
     * @param plugin the plugin
     */
    public static void unregister(final Plugin plugin) {
        for (final DataConverter<?> dataConverter : PLUGIN_DATA_CONVERTER_MAP.getOrDefault(plugin, Lists.newArrayList()))
            DATA_CONVERTER_BUFFER_MAP.remove(dataConverter);
        PLUGIN_DATA_CONVERTER_MAP.remove(plugin);
    }

    /**
     * Unregister all the getter of the buffers
     *
     * @return true if there are some getter of the buffers not belonging to MainPlugin not been unregistered, false otherwise
     */
    public static boolean unregisterAll() {
        boolean ret = false;
        for (final Plugin plugin : PLUGIN_DATA_CONVERTER_MAP.keySet()) {
            if (plugin != FocessQQ.getMainPlugin())
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
        for (final Class<?> c : this.buffers.keySet())
            this.buffers.get(c).flip();
    }

    /**
     * Get String argument in order
     *
     * @return the String argument in order
     * @throws NullPointerException if the value is null
     */
    @NonNull
    public String get() {
        return Objects.requireNonNull(this.get(String.class));
    }

    /**
     * Get int argument in order
     *
     * @return the int argument in order
     * @throws NullPointerException if the value is null
     */
    public int getInt() {
        return Objects.requireNonNull(this.get(Integer.class));
    }

    /**
     * Get double argument in order
     *
     * @return the double argument in order
     * @throws NullPointerException if the value is null
     */
    public double getDouble() {
        return Objects.requireNonNull(this.get(Double.class));
    }

    /**
     * Get boolean argument in order
     *
     * @return the boolean argument in order
     * @throws NullPointerException if the value is null
     */
    public boolean getBoolean() {
        return Objects.requireNonNull(this.get(Boolean.class));
    }

    /**
     * Get long argument in order
     *
     * @return the long argument in order
     * @throws NullPointerException if the value is null
     */
    public long getLong() {
        return Objects.requireNonNull(this.get(Long.class));
    }

    /**
     * Get Plugin argument in order
     *
     * @return the Plugin argument in order
     * @throws NullPointerException if the value is null
     */
    @NonNull
    public Plugin getPlugin() {
        return Objects.requireNonNull(this.get(Plugin.class));
    }

    /**
     * Get Command argument in order
     *
     * @return the Command argument in order
     */
    @NonNull
    public Command getCommand() {
        return Objects.requireNonNull(this.get(Command.class));
    }

    /**
     * Get buffer element
     *
     * @param cls the buffer elements' class
     * @param t   the default value
     * @param <T> the buffer elements' type
     * @return the buffer element
     * @throws UnsupportedOperationException if the buffer is not registered
     */
    @Contract("_,!null->!null")
    public <T> T getOrDefault(final Class<T> cls, final T t) {
        try {
            if (this.buffers.get(cls) == null)
                throw new UnsupportedOperationException();
            return (T) this.buffers.get(cls).get();
        } catch (final Exception e) {
            return t;
        }
    }

    /**
     * Get buffer element
     *
     * @param cls   the buffer elements' class
     * @param t     the default value
     * @param index the buffer element index
     * @param <T>   the buffer elements' type
     * @return the buffer element
     * @throws UnsupportedOperationException if the buffer is not registered
     */
    @Contract("_,_,!null->!null")
    public <T> T getOrDefault(final Class<T> cls, final int index, final T t) {
        try {
            if (this.buffers.get(cls) == null)
                throw new UnsupportedOperationException();
            return (T) this.buffers.get(cls).get(index);
        } catch (final Exception e) {
            return t;
        }
    }

    <T> void write(final Class<T> cls, final T t) {
        this.buffers.compute(cls, (key, value) -> {
            if (value == null)
                throw new UnsupportedOperationException();
            value.put(t);
            return value;
        });
    }

    /**
     * Get buffer element
     *
     * @param c   the buffer elements' class
     * @param <T> the buffer elements' type
     * @return the buffer element
     * @throws UnsupportedOperationException if the buffer is not registered
     */
    @Nullable
    public <T> T get(final Class<T> c) {
        if (this.buffers.get(c) == null)
            throw new UnsupportedOperationException();
        return (T) this.buffers.get(c).get();
    }

    /**
     * Get buffer element
     *
     * @param index the buffer element index
     * @param c     the buffer elements' class
     * @param <T>   the buffer elements' type
     * @return the buffer element
     * @throws UnsupportedOperationException if the buffer is not registered
     */
    @Nullable
    public <T> T get(final Class<T> c, final int index) {
        if (this.buffers.get(c) == null)
            throw new UnsupportedOperationException();
        return (T) this.buffers.get(c).get(index);
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
