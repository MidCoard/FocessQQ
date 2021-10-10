package com.focess.api.util.session;

import com.focess.api.util.SectionMap;
import com.google.common.collect.Maps;

import java.util.Map;

public class Session implements SectionMap {

    private final Map<String, Object> values;

    public Session(Map<String,Object> values) {
        this.values = values;
    }

    public Session() {
        this.values = Maps.newHashMap();
    }

    @Override
    public SectionMap createSection(String name) {
        Map<String,Object> values = Maps.newHashMap();
        this.values.put(name,values);
        return new SessionSection(this,values);
    }

    @Override
    public Map<String, Object> getValues() {
        return this.values;
    }

    @Override
    public SectionMap getSection(String name) {
        if (get(name) instanceof  Map)
            return new SessionSection(this,get(name));
        else throw new IllegalStateException("This " + name + " is not a valid section.");
    }
}
