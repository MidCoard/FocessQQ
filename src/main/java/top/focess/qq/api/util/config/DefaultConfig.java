package top.focess.qq.api.util.config;

import org.jetbrains.annotations.Nullable;
import top.focess.qq.api.util.yaml.YamlConfiguration;
import top.focess.qq.api.util.yaml.YamlLoadException;

import java.io.File;
import java.util.Map;

public class DefaultConfig extends Config {

    public DefaultConfig(final File file) throws YamlLoadException {
        super(file);
    }

    public DefaultConfig(final YamlConfiguration yamlConfiguration) {
        super(yamlConfiguration);
    }

    @Override
    public void set(final String key, @Nullable final Object value) {
        super.set(key, value);
    }

    @Override
    @Nullable
    public <T> T get(final String key) {
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
    public boolean contains(final String key) {
        return super.contains(key);
    }

    public <T> T getOrDefault(final String key, final T def) {
        return this.contains(key) ? this.get(key) : def;
    }

    @Override
    public void remove(final String key) {
        super.remove(key);
    }

    @Override
    public DefaultConfig getSection(final String key) {
        return new DefaultConfig(this.yaml.getSection(key));
    }
}
