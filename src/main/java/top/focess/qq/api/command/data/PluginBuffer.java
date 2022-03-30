package top.focess.qq.api.command.data;

import org.jetbrains.annotations.Nullable;
import top.focess.qq.api.plugin.Plugin;

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
    public static PluginBuffer allocate(final int size) {
        return new PluginBuffer(size);
    }

    @Override
    public void flip() {
        this.stringBuffer.flip();
    }

    @Override
    public void put(final Plugin plugin) {
        this.stringBuffer.put(plugin.getName());
    }

    @Nullable
    @Override
    public Plugin get() {
        return Plugin.getPlugin(this.stringBuffer.get());
    }

    @Nullable
    @Override
    public Plugin get(final int index) {
        return Plugin.getPlugin(this.stringBuffer.get(index));
    }
}
