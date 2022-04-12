package top.focess.qq.api.util.json;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;

/**
 * Represents a JSON object. It represents JSON or JSONList.
 */
public abstract class JSONObject {

    /**
     * Parse the given string into a JSON object.
     *
     * @param json the string to parse
     * @return the JSON object
     */
    @NotNull
    @Contract("_ -> new")
    public static JSONObject parse(final String json) {
        try {
            return new JSON(json);
        } catch (final Exception e) {
            return new JSONList(json);
        }
    }

    public static JSONObject parse(Object object) {
        if (object instanceof Map)
            return new JSON((Map) object);
        else if (object instanceof List)
            return new JSONList((List) object);
        else if (object instanceof String)
            return parse((String) object);
        throw new IllegalStateException("This element type is not supported.");
    }

    /**
     * Get the value at the given index
     * <p>
     * Note: this is for JSONList only.
     *
     * @param index the index of the value
     * @param <T>   the type of the value
     * @return the value at the given index
     */
    public <T> T get(final int index) {
        throw new UnsupportedOperationException();
    }

    /**
     * Get the value of the key-value pair
     * <p>
     * Note: this is for JSON only.
     *
     * @param key the key
     * @param <T> the type of the value
     * @return the value
     */
    public <T> T get(final String key) {
        throw new UnsupportedOperationException();
    }

    /**
     * Get the list at the given index
     * <p>
     * Note: this is for JSONList only.
     *
     * @param index the index of the list
     * @return the list at the given index
     */
    public JSONList getList(final int index) {
        throw new UnsupportedOperationException();
    }

    /**
     * Get the list named key
     * <p>
     * Note: this is for JSON only.
     *
     * @param key the key of the list
     * @return a list named key
     */
    public JSONList getList(final String key) {
        throw new UnsupportedOperationException();
    }

    /**
     * Get as JSON at the given index
     * <p>
     * Note: this is for JSONList only.
     *
     * @param index the index of the JSON
     * @return the JSON at the given index
     */
    public JSON getJSON(final int index) {
        throw new UnsupportedOperationException();
    }

    /**
     * Translate this JSON instance into json String
     *
     * @return json String translated from this JSON instance
     */
    public abstract String toJson();

    /**
     * Get the size of this JSON instance
     *
     * Note: this is for JSONList only.
     *
     * @return the size of this JSON instance
     */
    public int size() {
        throw new UnsupportedOperationException();
    }

}
