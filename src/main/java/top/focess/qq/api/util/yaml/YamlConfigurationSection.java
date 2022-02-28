package top.focess.qq.api.util.yaml;

import java.util.Map;

/**
 * Section of YamlConfiguration.
 */
public class YamlConfigurationSection extends YamlConfiguration {


    private final YamlConfiguration parent;

    public YamlConfigurationSection(YamlConfiguration parent, Map<String, Object> values) {
        super(values);
        this.parent = parent;
    }

    /**
     * Get the parent section
     *
     * @return the parent section
     */
    public YamlConfiguration getParent() {
        return parent;
    }

}
