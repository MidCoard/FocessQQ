package top.focess.qq.api.exceptions;

/**
 * Thrown to indicate JSON parsing error
 */
public class JSONParseException extends RuntimeException{

    /**
     * Constructs a new JSONParseException
     *
     * @param json the error parsed json
     */
    public JSONParseException(String json){
        super("Error in parsing JSON: " + json + ".");
    }
}
