package top.focess.qq.api.util.config;

import java.io.File;
import java.util.Map;

public class DefaultConfig extends Config {

    public DefaultConfig(File file) {
        super(file);
    }

    @Override
    public void set(String key,Object value) {
        super.set(key,value);
    }

    @Override
    public <T> T get(String key) {
        return super.get(key);
    }

    @Override
    public void save() {
        super.save();
    }

    @Override
    public Map<String, Object> getValues() {
        return super.getValues();
    }
}