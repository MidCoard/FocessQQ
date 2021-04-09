package com.focess.api.command.data;

import com.focess.api.Plugin;

public class PluginBuffer extends DataBuffer<Plugin> {

    private StringBuffer stringBuffer;


    public PluginBuffer(int size) {
        this.stringBuffer = StringBuffer.allocate(size);
    }

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
}
