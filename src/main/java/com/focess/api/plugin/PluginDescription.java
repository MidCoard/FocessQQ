package com.focess.api.plugin;

import com.focess.api.util.yaml.YamlConfiguration;

public class PluginDescription {


    private final YamlConfiguration pluginConfig;

    private final String main;

    public PluginDescription(YamlConfiguration pluginConfig) {
        this.pluginConfig = pluginConfig;
        this.main = this.pluginConfig.get("main");
    }

    public String getMain() {
        return main;
    }
}
