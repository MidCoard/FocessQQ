package top.focess.qq.api.util.config;

import org.checkerframework.checker.nullness.qual.Nullable;
import top.focess.util.yaml.YamlConfiguration;
import top.focess.util.yaml.YamlLoadException;

import java.io.File;
import java.io.InputStream;
import java.util.Map;

/**
 * Represents a configuration.
 */
public abstract class Config {

    protected final YamlConfiguration yaml;
    @Nullable
    private File file;

    /**
     * Loads the configuration from the given file
     *
     * Note: if the file is null or not exists, it will create an empty configuration.
     *
     * @param file the file
     * @throws YamlLoadException if the  file is not a valid yaml file
     */
    protected Config(@Nullable final File file) throws YamlLoadException {
        this.file = file;
        this.yaml = this.file != null && this.file.exists() ? YamlConfiguration.loadFile(file) : new YamlConfiguration(null);
    }

    /**
     * Loads the configuration from the given input stream
     * @param stream the given input stream
     */
    protected Config(@Nullable final InputStream stream) {
        this.file = null;
        this.yaml = YamlConfiguration.load(stream);
    }

    /**
     * Loads the configuration from the given values
     * @param values the given values
     */
    protected Config(@Nullable final Map<String, Object> values) {
        this.yaml = new YamlConfiguration(values);
    }

    /**
     * Loads the configuration from the given YamlConfiguration
     * @param yaml the given YamlConfiguration
     */
    protected Config(final YamlConfiguration yaml) {
        this.yaml = yaml;
    }

    /**
     * Get the file of this configuration
     * @return the file of this configuration or null if this configuration is not loaded from a file
     */
    @Nullable
    public File getFile() {
        return this.file;
    }

    /**
     * Get the value of the specified key
     *
     * @param key the key
     * @param <T> the value type
     * @return the value
     * @throws ClassCastException if the value is not the specified type
     */
    @Nullable
    protected <T> T get(final String key) {
        return this.yaml.get(key);
    }

    /**
     * Set the value of the specified key
     * @param key the key
     * @param value the value
     */
    protected void set(final String key, @Nullable final Object value) {
        this.yaml.set(key, value);
    }

    /**
     * Save the configuration to the file
     *
     * @throws UnsupportedOperationException if the configuration is not loaded from a file
     */
    protected void save() {
        if (this.file == null)
            throw new UnsupportedOperationException("File is null");
        this.yaml.save(this.file);
    }

    /**
     * Get all the configuration as a map
     *
     * @return all the configuration as a map
     */
    protected Map<String, Object> getValues() {
        return this.yaml.getValues();
    }

    /**
     * Indicate there is a configuration named key
     *
     * @param key the key
     * @return true there is a configuration named key, false otherwise
     */
    protected boolean contains(final String key) {
        return this.yaml.contains(key);
    }

    /**
     * Remove the configuration named key
     *
     * @param key the key
     */
    protected void remove(final String key) {
        this.yaml.remove(key);
    }

    /**
     * Get the section named key
     *
     * Note: if the section named key does not exist, it will be created
     *
     * @param key the key of the Section
     * @return the section named key
     * @throws UnsupportedOperationException if there is no section named key or the config does not support section
     */
    protected abstract Config getSection(String key);
}
