package com.focess.api.util.config;

import com.focess.api.util.yaml.YamlConfiguration;

import java.io.File;
import java.io.InputStream;

public class LangConfig extends Config {

    public LangConfig(InputStream inputStream) {
        super(null);
        this.yaml = inputStream != null ? YamlConfiguration.load(inputStream) : new YamlConfiguration(null);
    }

    public LangConfig(File file) {
        super(file);
    }

    @Override
    public <T> T get(String key) {
        return super.get(key);
    }
}
