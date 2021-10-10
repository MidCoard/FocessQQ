package com.focess.api.util.yaml;

import com.focess.Main;
import com.focess.api.util.SectionMap;
import com.focess.commands.LoadCommand;
import com.focess.api.util.Base64;
import com.google.common.collect.Maps;
import org.yaml.snakeyaml.Yaml;

import java.io.*;
import java.util.Map;

public class YamlConfiguration implements SectionMap {

    private static final Yaml YAML = new Yaml();
    private final Map<String, Object> values;

    public YamlConfiguration(Map<String, Object> values) {
        this.values = values == null ? Maps.newLinkedHashMap() : values;
    }

    public static YamlConfiguration loadFile(File file) {
        try {
            FileReader reader = new FileReader(file);
            YamlConfiguration yamlConfiguration = new YamlConfiguration(YAML.load(reader));
            reader.close();
            return yamlConfiguration;
        } catch (IOException e) {
            Main.getLogger().thr("Load Config File Exception",e);
        }
        return null;
    }

    @Override
    public YamlConfigurationSection createSection(String name) {
        Map<String,Object> values = Maps.newHashMap();
        this.values.put(name,values);
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
        if (value.getClass().isPrimitive() || value.getClass().equals(Double.class) || value.getClass().equals(Float.class) || value.getClass().equals(Short.class) || value.getClass().equals(Character.class) || value.getClass().equals(Long.class) || value.getClass().equals(Integer.class) || value.getClass().equals(Boolean.class) || value.getClass().equals(Byte.class))
            return (T) value;
        else {
            String str = (String) value;
            if (str.equals("null"))
                return null;
            else if (str.startsWith("!!")) {
                try {
                    LoadCommand.ObjectInputCoreStream inputStream = new LoadCommand.ObjectInputCoreStream(new ByteArrayInputStream(Base64.base64Decode(str.substring(2))));
                    T t = (T) inputStream.readObject();
                    inputStream.close();
                    return t;
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            } else return (T) value;
        }
    }

    @Override
    public Map<String, Object> getValues() {
        return this.values;
    }

    public void save(File file) {
        try {
            YAML.dump(this.values, new FileWriter(file));
        } catch (IOException e) {
            Main.getLogger().thr("Save Config File Exception",e);
        }
    }

    @Override
    public YamlConfigurationSection getSection(String key) {
        if (get(key) instanceof Map)
            return new YamlConfigurationSection(this, get(key));
        else throw new IllegalStateException("This " + key + " is not a valid section.");
    }
}