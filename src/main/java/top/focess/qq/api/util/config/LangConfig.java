package top.focess.qq.api.util.config;

import top.focess.qq.FocessQQ;
import top.focess.qq.api.util.yaml.YamlLoadException;
import top.focess.qq.api.util.yaml.YamlConfiguration;

import java.io.File;
import java.io.InputStream;

public class LangConfig extends Config {

    public LangConfig(InputStream inputStream) throws YamlLoadException {
        super(null);
        this.yaml = inputStream != null ? YamlConfiguration.load(inputStream) : new YamlConfiguration(null);
    }

    public LangConfig(File file) throws YamlLoadException {
        super(file);
    }

    @Override
    public String get(String key) {
        String ret = super.get(key);
        if (ret == null)
            FocessQQ.getLogger().debugLang("unknown-key", key);
        return ret != null ? ret : "";
    }
}
