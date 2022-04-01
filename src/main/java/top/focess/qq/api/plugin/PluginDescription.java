package top.focess.qq.api.plugin;

import org.jetbrains.annotations.NotNull;
import top.focess.qq.api.util.yaml.YamlConfiguration;

/**
 * The description of plugin.
 */
public class PluginDescription {

    /**
     * The plugin class path
     */
    private final String main;

    /**
     * Constructs a new PluginDescription from the plugin config
     *
     * @param pluginConfig the plugin config named "plugin.yml"
     */
    public PluginDescription(@NotNull final YamlConfiguration pluginConfig) {
        this.main = pluginConfig.get("main");
    }

    PluginDescription() {
        this.main = "top.focess.qq.FocessQQ$MainPlugin";
    }

    public String getMain() {
        return this.main;
    }
}
