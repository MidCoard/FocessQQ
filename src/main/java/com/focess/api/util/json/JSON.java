package com.focess.api.util.json;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.focess.api.exceptions.JSONParseException;
import com.focess.api.util.SectionMap;
import com.google.common.collect.Maps;

import java.io.IOException;
import java.util.Map;

public class JSON implements SectionMap {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final TypeReference<Map<String,Object>> TYPE_REFERENCE =  new TypeReference<Map<String,Object>>(){};

    private final Map<String,Object> values;

    public JSON(String json) {
        try {
            this.values = OBJECT_MAPPER.readValue(json,TYPE_REFERENCE);
        } catch (IOException e) {
            throw new JSONParseException(json);
        }
    }

    public JSON(Map<String, Object> values) {
        this.values = values;
    }

    @Override
    public JSONSection createSection(String name) {
        Map<String,Object> values = Maps.newHashMap();
        this.values.put(name,values);
        return new JSONSection(this,values);
    }

    @Override
    public Map<String, Object> getValues() {
        return this.values;
    }

    @Override
    public JSONSection getSection(String name) {
        if (get(name) instanceof Map)
            return new JSONSection(this,get(name));
        else throw new IllegalStateException("This " + name + " is not a valid section.");
    }

    public String toJson() {
        try {
            return OBJECT_MAPPER.writeValueAsString(this.values);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

}
