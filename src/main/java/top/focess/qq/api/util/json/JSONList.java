package top.focess.qq.api.util.json;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * This class is used to define a JSON object as List.
 */
public class JSONList extends JSONObject {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final TypeReference<List<Object>> TYPE_REFERENCE =  new TypeReference<List<Object>>(){};
    private final List<?> values;


    public JSONList(String json) {
        try {
            this.values = OBJECT_MAPPER.readValue(json,TYPE_REFERENCE);
        } catch (IOException e) {
            throw new JSONParseException(json);
        }
    }

    public JSONList(List<?> values) {
        this.values = values;
    }

    public <T> T get(int index) {
        return (T) values.get(index);
    }

    public JSON getJSON(int index) {
        if (values.get(index) instanceof Map)
            return new JSON((Map<String,Object>) values.get(index));
        throw new IllegalStateException("This element is not a valid map.");
    }

    public JSONList getList(int index) {
        if (values.get(index) instanceof List)
            return new JSONList((List<?>) values.get(index));
        throw new IllegalStateException("This element is not a valid list.");
    }

    public List<?> getValues() {
        return this.values;
    }

    public String toJson() {
        try {
            return OBJECT_MAPPER.writeValueAsString(this.values);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String toString() {
        return values.toString();
    }
}
