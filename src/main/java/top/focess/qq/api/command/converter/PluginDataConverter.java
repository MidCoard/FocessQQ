package top.focess.qq.api.command.converter;

import org.jetbrains.annotations.Nullable;
import top.focess.qq.api.plugin.Plugin;

/**
 * Convert the String argument to Plugin argument
 */
public class PluginDataConverter extends NullDataConverter<Plugin> {

    /**
     * Convert the String argument to Plugin argument
     */
    public static final PluginDataConverter PLUGIN_DATA_CONVERTER = new PluginDataConverter();

    @Nullable
    @Override
    public Plugin convert(String arg) {
        return Plugin.getPlugin(arg);
    }

    @Override
    protected Class<Plugin> getTargetClass() {
        return Plugin.class;
    }
}