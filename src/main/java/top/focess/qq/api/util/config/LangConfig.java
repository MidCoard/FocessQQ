package top.focess.qq.api.util.config;

import org.checkerframework.checker.nullness.qual.Nullable;
import top.focess.qq.FocessQQ;
import top.focess.util.yaml.YamlLoadException;

import java.io.File;
import java.io.InputStream;

/**
 * Represents a language config named "lang.yml" in the plugin jar file.
 */
public class LangConfig extends Config {

    public LangConfig(@Nullable final InputStream inputStream) {
        super(inputStream);
    }

    public LangConfig(final File file) throws YamlLoadException {
        super(file);
    }

    @Override
    protected Config getSection(final String key) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String get(final String key) {
        final String ret = super.get(key);
        if (ret == null)
            FocessQQ.getLogger().debugLang("unknown-key", key);
        return ret != null ? ret : "";
    }
}
