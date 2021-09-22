package com.focess.api.util;

import com.focess.util.yaml.YamlConfigurationSection;
import org.apache.poi.hpsf.Section;

import java.util.Map;
import java.util.Set;

public interface SectionMap {

    default void set(String name, Object value) {
        this.getValues().put(name,value);
    }

    default <T> T get(String name) {
        return (T) this.getValues().get(name);
    }
    default boolean contains(String name) {
        return this.getValues().containsKey(name);
    }

    default void remove(String name) {
        this.getValues().remove(name);
    }

    SectionMap createSection(String name);

    Map<String,Object> getValues();

    default Set<String> keys() {
        return this.getValues().keySet();
    }

    SectionMap getSection(String name);
}
