package com.focess.api.plugin;

import com.focess.api.util.yaml.YamlConfiguration;

public class PluginDescription {

    private final String main;

    public PluginDescription(YamlConfiguration pluginConfig) {
        this.main = pluginConfig.get("main");
    }

    PluginDescription() {
        this.main = "com.focess.Main$MainPlugin";
    }

    public String getMain() {
        return main;
    }
}
