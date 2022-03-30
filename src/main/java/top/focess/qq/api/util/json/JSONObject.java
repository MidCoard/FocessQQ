package top.focess.qq.api.util.json;

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
    public static JSONObject parse(final String json) {
        try {
            return new JSON(json);
        } catch (final Exception e) {
            return new JSONList(json);
        }
    }

    /**
     * Get the value at the given index
     *
     * Note: This is for JSONList only.
     *
     * @param index the index of the value
     * @param <T> the type of the value
     * @return the value at the given index
     */
    public <T> T get(final int index) {
        throw new UnsupportedOperationException();
    }

    /**
     * Get the value of the key-value pair
     *
     * Note: This is for JSON only.
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
     *
     * Note: This is for JSONList only.
     *
     * @param index the index of the list
     * @return the list at the given index
     */
    public JSONObject getList(final int index) {
        throw new UnsupportedOperationException();
    }

    /**
     * Get the list named key
     *
     * Note: This is for JSON only.
     *
     * @param key the key of the list
     * @return a list named key
     */
    public JSONObject getList(final String key) {
        throw new UnsupportedOperationException();
    }

    /**
     * Get as JSON at the given index
     *
     * Note: This is for JSONList only.
     *
     * @param index the index of the JSON
     * @return the JSON at the given index
     */
    public JSONObject getJSON(final int index) {
        throw new UnsupportedOperationException();
    }

    /**
     * Translate this JSON instance into json String
     *
     * @return json String translated from this JSON instance
     */
    public abstract String toJson();

}
