package com.focess.api.util.config;

import com.focess.api.util.yaml.YamlConfiguration;

import java.io.File;

public abstract class Config {

    private final File file;
    private final YamlConfiguration yaml;

    public Config(File file) {
        this.file = file;
        this.yaml = this.file.exists() ? YamlConfiguration.loadFile(file) : new YamlConfiguration(null);
    }

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
}
