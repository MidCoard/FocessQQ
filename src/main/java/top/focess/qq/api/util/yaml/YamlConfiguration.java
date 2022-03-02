package top.focess.qq.api.util.yaml;

import top.focess.qq.FocessQQ;
import top.focess.qq.api.exceptions.YamlLoadException;
import top.focess.qq.api.util.Base64;
import top.focess.qq.api.util.SectionMap;
import top.focess.qq.core.plugin.ObjectInputCoreStream;
import com.google.common.collect.Maps;
import org.jetbrains.annotations.Nullable;
import org.yaml.snakeyaml.Yaml;

import java.io.*;
import java.util.Map;

/**
 * This class is used to define a YAML configuration.
 */
public class YamlConfiguration implements SectionMap {

    private static final Yaml YAML = new Yaml();
    private final Map<String, Object> values;

    /**
     * Initialize the YamlConfiguration with existed key-value pairs or not
     *
     * @param values the YAML configuration key-value pairs
     */
    public YamlConfiguration(@Nullable Map<String, Object> values) {
        this.values = values == null ? Maps.newHashMap() : values;
    }

    /**
     * Load the file as a YAML configuration
     *
     * @param file where to load
     * @return YAML configuration
     */
    public static YamlConfiguration loadFile(File file) {
        try {
            FileReader reader = new FileReader(file);
            YamlConfiguration yamlConfiguration = new YamlConfiguration(YAML.load(reader));
            reader.close();
            return yamlConfiguration;
        } catch (IOException e) {
            FocessQQ.getLogger().thrLang("exception-load-file",e);
        }
        return null;
    }

    public static YamlConfiguration load(InputStream inputStream) {
        return new YamlConfiguration(YAML.load(inputStream));
    }

    @Override
    public YamlConfigurationSection createSection(String key) {
        Map<String,Object> values = Maps.newHashMap();
        this.values.put(key,values);
        return new YamlConfigurationSection(this,values);
    }

    @Override
    public void set(String key, Object value) {
        if (value == null) {
            values.put(key,"null");
        } else if (value.getClass().isPrimitive() || value.getClass().equals(Double.class) || value.getClass().equals(Float.class) || value.getClass().equals(Short.class) || value.getClass().equals(Character.class) || value.getClass().equals(Long.class) || value.getClass().equals(Integer.class) || value.getClass().equals(Boolean.class) || value.getClass().equals(Byte.class) || value.getClass().equals(String.class)) {
            values.put(key, value);
        }
        else {
            try {
                ByteArrayOutputStream stream;
                ObjectOutputStream outputStream = new ObjectOutputStream(stream = new ByteArrayOutputStream());
                outputStream.writeObject(value);
                outputStream.close();
                values.put(key,"!!" + Base64.base64Encode(stream.toByteArray()));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public <T> T get(String key) {
        Object value = SectionMap.super.get(key);
        if (value == null)
            return null;
        if (value.getClass().isPrimitive() || value.getClass().equals(Double.class) || value.getClass().equals(Float.class) || value.getClass().equals(Short.class) || value.getClass().equals(Character.class) || value.getClass().equals(Long.class) || value.getClass().equals(Integer.class) || value.getClass().equals(Boolean.class) || value.getClass().equals(Byte.class))
            return (T) value;
        else {
            String str = (String) value;
            if (str.equals("null"))
                return null;
            else if (str.startsWith("!!")) {
                try {
                    ObjectInputCoreStream inputStream = new ObjectInputCoreStream(new ByteArrayInputStream(Base64.base64Decode(str.substring(2))));
                    T t = (T) inputStream.readObject();
                    inputStream.close();
                    return t;
                } catch (Exception e) {
                    throw new YamlLoadException(e);
                }
            } else return (T) value;
        }
    }

    @Override
    public Map<String, Object> getValues() {
        return this.values;
    }

    /**
     * Save the YAML configuration as a file
     *
     * @param file where to save
     */
    public void save(File file) {
        try {
            YAML.dump(this.values, new FileWriter(file));
        } catch (IOException e) {
            FocessQQ.getLogger().thrLang("exception-save-file",e);
        }
    }

    @Override
    public YamlConfigurationSection getSection(String key) {
        if (get(key) instanceof Map)
            return new YamlConfigurationSection(this, get(key));
        else throw new IllegalStateException("This " + key + " is not a valid section.");
    }

    @Override
    public String toString() {
        return values.toString();
    }
}
