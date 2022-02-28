package com.focess.api.util.session;

import com.focess.api.util.SectionMap;
import com.google.common.collect.Maps;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

/**
 * This class is used to build better communication between Command and CommandSender. It can save something in the executing process and can be used for future.
 */
public class Session implements SectionMap {

    private final Map<String, Object> values;

    /**
     * Initialize the YamlConfiguration with existed key-value pairs or not (usually not)
     *
     * @param values the session key-value pairs
     */
    public Session(@Nullable Map<String,Object> values) {
        this.values = values == null ? Maps.newHashMap() : values;
    }

    @Override
    public SectionMap createSection(String key) {
        Map<String,Object> values = Maps.newHashMap();
        this.values.put(key,values);
        return new SessionSection(this,values);
    }

    @Override
    public Map<String, Object> getValues() {
        return this.values;
    }

    @Override
    public SectionMap getSection(String key) {
        if (get(key) instanceof  Map)
            return new SessionSection(this,get(key));
        else throw new IllegalStateException("This " + key + " is not a valid section.");
    }

    @Override
    public String toString() {
        return values.toString();
    }
}
