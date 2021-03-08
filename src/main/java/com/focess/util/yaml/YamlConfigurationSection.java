package com.focess.util.yaml;

import java.util.Map;

public class YamlConfigurationSection extends YamlConfiguration {


    private final YamlConfiguration parent;

    public YamlConfigurationSection(YamlConfiguration parent, Map<String, Object> values) {
        super(values);
        this.parent = parent;
    }

    public YamlConfiguration getParent() {
        return parent;
    }

}
