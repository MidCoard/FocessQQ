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
    public String get(String key) {
        String ret = super.get(key);
        return ret != null ? ret : "";
    }
}
