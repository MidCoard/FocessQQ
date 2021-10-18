package com.focess.api.util.json;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.focess.api.exceptions.JSONParseException;

import java.io.IOException;
import java.util.List;

/**
 * This class is used to define a JSON object as List.
 */
public class ListJSON {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final TypeReference<List<Object>> TYPE_REFERENCE =  new TypeReference<List<Object>>(){};
    private final List<?> values;


    public ListJSON(String json) {
        try {
            this.values = OBJECT_MAPPER.readValue(json,TYPE_REFERENCE);
        } catch (IOException e) {
            throw new JSONParseException(json);
        }
    }

    public ListJSON(List<?> values) {
        this.values = values;
    }

    public List<?> getValues() {
        return this.values;
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
