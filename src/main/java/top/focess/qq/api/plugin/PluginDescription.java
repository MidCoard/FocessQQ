package top.focess.qq.api.plugin;

import top.focess.qq.api.util.yaml.YamlConfiguration;

public class PluginDescription {

    private final String main;

    public PluginDescription(final YamlConfiguration pluginConfig) {
        this.main = pluginConfig.get("main");
    }

    PluginDescription() {
        this.main = "top.focess.qq.FocessQQ$MainPlugin";
    }

    public String getMain() {
        return this.main;
    }
}
