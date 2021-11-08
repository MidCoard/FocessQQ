package com.focess.api.util.json;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.focess.api.exceptions.JSONParseException;
import com.focess.api.util.SectionMap;
import com.google.common.collect.Maps;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * This class is used to define a JSON object as Map.
 */
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
    public JSONSection createSection(String key) {
        Map<String,Object> values = Maps.newHashMap();
        this.values.put(key,values);
        return new JSONSection(this,values);
    }

    @Override
    public Map<String, Object> getValues() {
        return this.values;
    }

    @Override
    public JSONSection getSection(String key) {
        if (get(key) instanceof Map)
            return new JSONSection(this,get(key));
        else throw new IllegalStateException("This " + key + " is not a valid section.");
    }

    /**
     * Get the list named key
     *
     * @param key the key of the list
     * @return a list named key
     */
    public JSONList getList(String key) {
        if (get(key) instanceof List)
            return new JSONList(this.<List<?>>get(key));
        else throw new IllegalStateException("This " + key + " is not a valid list.");
    }

    /**
     * Translate this JSON instance into json String
     *
     * @return json String translated from this JSON instance
     */
    public String toJson() {
        try {
            return OBJECT_MAPPER.writeValueAsString(this.values);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

}
