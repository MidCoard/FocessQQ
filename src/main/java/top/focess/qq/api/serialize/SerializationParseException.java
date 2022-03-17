package top.focess.qq.api.serialize;

/**
 * Thrown to indicate that a serialization parse error has occurred.
 */
public class SerializationParseException extends RuntimeException {
    /**
     * Constructs a SerializationParseException with the specified detail message
     * @param message the detail message
     */
    public SerializationParseException(String message) {
        super(message);
    }

    /**
     * Constructs a SerializationParseException with the cause
     * @param e the cause
     */
    public SerializationParseException(Exception e) {
        super(e);
    }
}
