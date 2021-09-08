package com.focess.util.yaml;

import com.focess.Main;
import com.google.common.collect.Maps;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Map;

public class YamlConfiguration {

    private static final Yaml YAML = new Yaml();
    private final Map<String, Object> values;

    public YamlConfiguration(Map<String, Object> values) {
        this.values = values == null ? Maps.newLinkedHashMap() : values;
    }

    public static YamlConfiguration loadFile(File file) {
        try {
            FileReader reader = new FileReader(file);
            YamlConfiguration yamlConfiguration = new YamlConfiguration(YAML.load(reader));
            reader.close();
            return yamlConfiguration;
        } catch (IOException e) {
            Main.getLogger().thr("Load Config File Exception",e);
        }
        return null;
    }

    public void set(String key, Object value) {
        values.put(key, value);
    }

    public <T> T get(String key) {
        return (T) values.get(key);
    }

    public boolean contains(String key) {
        return this.values.containsKey(key);
    }

    public Map<String, Object> getValues() {
        return this.values;
    }

    public void save(File file) {
        try {
            YAML.dump(this.values, new FileWriter(file));
        } catch (IOException e) {
            Main.getLogger().thr("Save Config File Exception",e);
        }
    }

    public YamlConfigurationSection getSection(String key) {
        if (get(key) instanceof Map)
            return new YamlConfigurationSection(this, get(key));
        else throw new IllegalStateException("This " + key + " is not a valid section.");
    }
}
