package com.focess.api.util;

import java.io.Serializable;
import java.util.Map;
import java.util.Set;

public interface SectionMap extends Serializable {

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
