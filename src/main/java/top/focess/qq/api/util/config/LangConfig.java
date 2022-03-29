package top.focess.qq.api.util.config;

import org.checkerframework.checker.nullness.qual.Nullable;
import top.focess.qq.FocessQQ;
import top.focess.qq.api.util.yaml.YamlLoadException;

import java.io.File;
import java.io.InputStream;

public class LangConfig extends Config {

    public LangConfig(@Nullable InputStream inputStream) {
        super(inputStream);
    }

    @Override
    protected Config getSection(String key) {
        throw new UnsupportedOperationException();
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
