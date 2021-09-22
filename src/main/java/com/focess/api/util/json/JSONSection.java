package com.focess.api.util.json;

import com.focess.api.util.SectionMap;

import java.util.Map;

public class JSONSection extends JSON {
    private final JSON parent;

    public JSONSection(JSON parent, Map<String,Object> values) {
        super(values);
        this.parent = parent;
    }

    public JSON getParent() {
        return parent;
    }
}
