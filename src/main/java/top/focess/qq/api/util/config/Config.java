package top.focess.qq.api.util.config;

import org.jetbrains.annotations.Nullable;
import top.focess.qq.api.util.yaml.YamlConfiguration;

import java.io.File;
import java.util.Map;

public abstract class Config {

    @Nullable
    private final File file;

    protected YamlConfiguration yaml;

    protected Config(@Nullable File file) {
        this.file = file;
        this.yaml = this.file != null && this.file.exists() ? YamlConfiguration.loadFile(file) : new YamlConfiguration(null);
    }

    @Nullable
    public File getFile() {
        return file;
    }

    protected <T> T get(String key) {
        return this.yaml.get(key);
    }

    protected void set(String key,Object value) {
        this.yaml.set(key, value);
    }

    protected void save() {
        this.yaml.save(file);
    }

    protected Map<String,Object> getValues() {
        return this.yaml.getValues();
    }
}
