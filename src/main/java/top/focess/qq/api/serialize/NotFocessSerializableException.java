package top.focess.qq.api.serialize;

/**
 * Thrown to indicate that an object is not serializable.
 */
public class NotFocessSerializableException extends RuntimeException {

    /**
     * Constructs a NotFocessSerializableException
     * @param cls the class that is not serializable
     */
    public NotFocessSerializableException(final String cls) {
        super("The class " + cls + " is not FocessSerializable");
    }
}
