package top.focess.qq.api.command.data;

import top.focess.qq.api.plugin.Plugin;

/**
 * Represent a buffer of Plugin.
 */
public class PluginBuffer extends DataBuffer<Plugin> {

    private final StringBuffer stringBuffer;

    private PluginBuffer(int size) {
        this.stringBuffer = StringBuffer.allocate(size);
    }

    /**
     * Allocate a PluginBuffer with fixed size
     *
     * @param size the target buffer size
     * @return a PluginBuffer with fixed size
     */
    public static PluginBuffer allocate(int size) {
        return new PluginBuffer(size);
    }

    @Override
    public void flip() {
        stringBuffer.flip();
    }

    @Override
    public void put(Plugin plugin) {
        stringBuffer.put(plugin.getName());
    }

    @Override
    public Plugin get() {
        return Plugin.getPlugin(stringBuffer.get());
    }

    @Override
    public Plugin get(int index) {
        return Plugin.getPlugin(stringBuffer.get(index));
    }
}
