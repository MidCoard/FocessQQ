package top.focess.qq.api.command.data;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import top.focess.command.data.DataBuffer;
import top.focess.command.data.StringBuffer;
import top.focess.qq.api.plugin.Plugin;

import java.util.Objects;

/**
 * Represent a buffer of Plugin.
 */
public class PluginBuffer extends DataBuffer<Plugin> {

    private final StringBuffer stringBuffer;

    private PluginBuffer(final int size) {
        this.stringBuffer = StringBuffer.allocate(size);
    }

    /**
     * Allocate a PluginBuffer with fixed size
     *
     * @param size the target buffer size
     * @return a PluginBuffer with fixed size
     */
    @NotNull
    @Contract("_ -> new")
    public static PluginBuffer allocate(final int size) {
        return new PluginBuffer(size);
    }

    @Override
    public void flip() {
        this.stringBuffer.flip();
    }

    @Override
    public void put(@NotNull final Plugin plugin) {
        this.stringBuffer.put(plugin.getName());
    }

    @NotNull
    @Override
    public Plugin get() {
        final String name = this.stringBuffer.get();
        try {
            return Objects.requireNonNull(Plugin.getPlugin(name));
        } catch (final NullPointerException e) {
            throw new IllegalArgumentException("Plugin: " + name + " is not found");
        }
    }

    @NotNull
    @Override
    public Plugin get(final int index) {
        final String name = this.stringBuffer.get(index);
        try {
            return Objects.requireNonNull(Plugin.getPlugin(name));
        } catch (final NullPointerException e) {
            throw new IllegalArgumentException("Plugin: " + name + " is not found");
        }
    }
}
