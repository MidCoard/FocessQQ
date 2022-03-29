package top.focess.qq.api.util.config;

import top.focess.qq.api.util.yaml.YamlConfiguration;
import top.focess.qq.api.util.yaml.YamlLoadException;

import java.io.File;
import java.util.Map;

public class DefaultConfig extends Config {

    public DefaultConfig(File file) throws YamlLoadException {
        super(file);
    }

    public DefaultConfig(YamlConfiguration yamlConfiguration) {
        super(yamlConfiguration);
    }

    @Override
    public void set(String key,Object value) {
        super.set(key,value);
    }

    @Override
    public <T> T get(String key) {
        return super.get(key);
    }

    @Override
    public void save() {
        super.save();
    }

    @Override
    public Map<String, Object> getValues() {
        return super.getValues();
    }

    @Override
    public boolean contains(String key) {
        return super.contains(key);
    }

    public <T> T getOrDefault(String key,T def) {
        return contains(key) ? get(key) : def;
    }

    @Override
    public void remove(String key) {
        super.remove(key);
    }

    @Override
    public DefaultConfig getSection(String key) {
        return new DefaultConfig(this.yaml.getSection(key));
    }
}
