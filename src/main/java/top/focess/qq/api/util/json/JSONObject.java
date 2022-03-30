package top.focess.qq.api.util.json;

/**
 * Represents a JSON object. It includes JSON and JSONList.
 */
public abstract class JSONObject {

    public static JSONObject parse(String json) {
        try {
            return new JSON(json);
        } catch (Exception e) {
            return new JSONList(json);
        }
    }

    public <T> T get(int index) {
        throw new UnsupportedOperationException();
    }

    public <T> T get(String key) {
        throw new UnsupportedOperationException();
    }

    public JSONObject getList(int index) {
        throw new UnsupportedOperationException();
    }

    public JSONObject getList(String key) {
        throw new UnsupportedOperationException();
    }

    public JSONObject getJSON(int index) {
        throw new UnsupportedOperationException();
    }

}
