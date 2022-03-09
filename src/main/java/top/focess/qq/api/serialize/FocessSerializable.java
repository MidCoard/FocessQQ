package top.focess.qq.api.serialize;

import java.io.Serializable;
import java.util.Map;

/**
 * Represents this class is a serializable class
 *
 * You should implement the deserialize method if you have implemented the serialize method (not return null).
 *
 */
public interface FocessSerializable extends Serializable {

    /**
     * Serialize the object
     *
     * @return the serialized object, null if it should serialize all fields in the object.
     */
    default Map<String,Object> serialize() {
        return null;
    }
}
