package top.focess.qq.api.serialize;

/**
 * Thrown to indicate that a serialization parse error has occurred.
 */
public class SerializationParseException extends RuntimeException {
    /**
     * Constructs a SerializationParseException
     *
     * @param message the detail message
     */
    public SerializationParseException(final String message) {
        super(message);
    }

    /**
     * Constructs a SerializationParseException
     *
     * @param e the cause
     */
    public SerializationParseException(final Exception e) {
        super(e);
    }
}
