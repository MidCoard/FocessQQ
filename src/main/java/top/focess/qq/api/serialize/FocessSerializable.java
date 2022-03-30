package top.focess.qq.api.serialize;

import org.jetbrains.annotations.Nullable;

import java.io.Serializable;
import java.util.Map;

/**
 * Represents this class is a serializable class
 * <p>
 * You should implement the deserialize method if you have implemented the serialize method (not return null).
 */
public interface FocessSerializable extends Serializable {

    /**
     * Serialize the object
     *
     * @return the serialized object, null if it should serialize all fields in the object.
     */
    @Nullable
    default Map<String, Object> serialize() {
        return null;
    }
}
