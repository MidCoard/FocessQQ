package top.focess.qq.api.util.config;

import org.checkerframework.checker.nullness.qual.Nullable;
import top.focess.qq.api.util.yaml.YamlConfiguration;
import top.focess.qq.api.util.yaml.YamlLoadException;

import java.io.File;
import java.io.InputStream;
import java.util.Map;

public abstract class Config {

    @Nullable
    private final File file;

    protected YamlConfiguration yaml;

    protected Config(@Nullable File file) throws YamlLoadException {
        this.file = file;
        this.yaml = this.file != null && this.file.exists() ? YamlConfiguration.loadFile(file) : new YamlConfiguration(null);
    }

    protected Config(@Nullable InputStream stream) {
        this.file = null;
        this.yaml = YamlConfiguration.load(stream);
    }

    @Nullable
    public File getFile() {
        return file;
    }

    @Nullable
    protected <T> T get(String key) {
        return this.yaml.get(key);
    }

    protected void set(String key,Object value) {
        this.yaml.set(key, value);
    }

    protected void save() {
        if (file == null)
            throw new UnsupportedOperationException("File is null");
        this.yaml.save(file);
    }

    protected Map<String,Object> getValues() {
        return this.yaml.getValues();
    }

    protected boolean contains(String key) {
        return this.yaml.contains(key);
    }

    protected void remove(String key) {
        this.yaml.remove(key);
    }
}
