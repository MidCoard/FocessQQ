package top.focess.qq.api.command.converter;

import top.focess.qq.api.plugin.Plugin;
import top.focess.qq.api.command.DataCollection;

/**
 * Convert the String argument to Plugin argument
 */
public class PluginDataConverter extends NullDataConverter<Plugin> {

    /**
     * Convert the String argument to Plugin argument
     */
    public static final PluginDataConverter PLUGIN_DATA_CONVERTER = new PluginDataConverter();

    @Override
    public Plugin convert(String arg) {
        return Plugin.getPlugin(arg);
    }

    @Override
    protected void connect(DataCollection dataCollection, Plugin arg) {
        dataCollection.writePlugin(arg);
    }

    @Override
    protected Class<Plugin> getTargetClass() {
        return Plugin.class;
    }
}