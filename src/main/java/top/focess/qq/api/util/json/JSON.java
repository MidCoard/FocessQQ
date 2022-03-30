package top.focess.qq.api.util.json;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import top.focess.qq.api.util.SectionMap;
import com.google.common.collect.Maps;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * This class is used to define a JSON object as Map.
 */
public class JSON extends JSONObject implements SectionMap {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final TypeReference<Map<String,Object>> TYPE_REFERENCE =  new TypeReference<Map<String,Object>>(){};

    private final Map<String,Object> values;

    public JSON(final String json) {
        try {
            this.values = OBJECT_MAPPER.readValue(json,TYPE_REFERENCE);
        } catch (final IOException e) {
            throw new JSONParseException(json);
        }
    }

    public JSON(final Map<String, Object> values) {
        this.values = values;
    }

    @Override
    public JSONSection createSection(final String key) {
        final Map<String,Object> values = Maps.newHashMap();
        this.set(key,values);
        return new JSONSection(this,values);
    }

    @Override
    public Map<String, Object> getValues() {
        return this.values;
    }

    @Override
    public JSONSection getSection(final String key) {
        final Object value = this.get(key);
        if (value == null)
            this.createSection(key);
        if (value instanceof Map)
            return new JSONSection(this, (Map<String, Object>) value);
        throw new IllegalStateException("This " + key + " is not a valid section.");
    }

    @Override
    public boolean containsSection(final String key) {
        return this.get(key) instanceof Map;
    }

    public JSONList getList(final String key) {
        if (this.get(key) instanceof List)
            return new JSONList(this.<List<?>>get(key));
        else throw new IllegalStateException("This " + key + " is not a valid list.");
    }

    public String toJson() {
        try {
            return OBJECT_MAPPER.writeValueAsString(this.values);
        } catch (final JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public <T> T get(final String key) {
        return SectionMap.super.get(key);
    }

    @Override
    public String toString() {
        return this.values.toString();
    }
}
