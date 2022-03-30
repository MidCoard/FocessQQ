package top.focess.qq.api.util.config;

import org.checkerframework.checker.nullness.qual.Nullable;
import top.focess.qq.api.util.yaml.YamlConfiguration;
import top.focess.qq.api.util.yaml.YamlLoadException;

import java.io.File;
import java.io.InputStream;
import java.util.Map;

public abstract class Config {

    protected final YamlConfiguration yaml;
    @Nullable
    private File file;

    protected Config(@Nullable final File file) throws YamlLoadException {
        this.file = file;
        this.yaml = this.file != null && this.file.exists() ? YamlConfiguration.loadFile(file) : new YamlConfiguration(null);
    }

    protected Config(@Nullable final InputStream stream) {
        this.file = null;
        this.yaml = YamlConfiguration.load(stream);
    }

    protected Config(@Nullable final Map<String, Object> values) {
        this.yaml = new YamlConfiguration(values);
    }

    protected Config(final YamlConfiguration yaml) {
        this.yaml = yaml;
    }

    @Nullable
    public File getFile() {
        return this.file;
    }

    @Nullable
    protected <T> T get(final String key) {
        return this.yaml.get(key);
    }

    protected void set(final String key, @Nullable final Object value) {
        this.yaml.set(key, value);
    }

    protected void save() {
        if (this.file == null)
            throw new UnsupportedOperationException("File is null");
        this.yaml.save(this.file);
    }

    protected Map<String, Object> getValues() {
        return this.yaml.getValues();
    }

    protected boolean contains(final String key) {
        return this.yaml.contains(key);
    }

    protected void remove(final String key) {
        this.yaml.remove(key);
    }

    protected abstract Config getSection(String key);
}
