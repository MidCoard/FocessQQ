package top.focess.qq.api.util.json;

/**
 * Thrown to indicate JSON parsing error
 */
public class JSONParseException extends RuntimeException {

    /**
     * Constructs a new JSONParseException
     *
     * @param json the error parsed json
     */
    public JSONParseException(final String json) {
        super("Error in parsing JSON: " + json + ".");
    }
}
