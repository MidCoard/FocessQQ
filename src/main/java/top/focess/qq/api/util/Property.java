package top.focess.qq.api.util;

import top.focess.qq.FocessQQ;

/**
 * Store some default properties of this framework.
 */
public class Property {

    private Property() {
    }

    /**
     * Indicate whether it has the key or not
     *
     * @param key the FocessQQ framework properties' key
     * @return true if it has the key, false otherwise
     */
    public static boolean hasKey(final String key) {
        return FocessQQ.MainPlugin.getProperties().containsKey(key);
    }

    /**
     * Set the property
     *
     * @param key   the FocessQQ framework properties' key
     * @param value the FocessQQ framework properties' value
     */
    public static void put(final String key, final Object value) {
        FocessQQ.MainPlugin.getProperties().put(key, value);
    }

    /**
     * Get the value of the key in the properties
     *
     * @param key the FocessQQ framework properties' key
     * @param <T> the value type
     * @return the value
     * @throws ClassCastException if the value is not the specified type
     */
    public static <T> T get(final String key) {
        return (T) FocessQQ.MainPlugin.getProperties().get(key);
    }

    /**
     * Get the value of the key in the properties or default value if the key is not existed
     *
     * @param key the FocessQQ framework properties' key
     * @param t   the default value
     * @param <T> the value type
     * @return the value, or the default value if there is no value
     *
     */
    public static <T> T getOrDefault(final String key, final T t) {
        return (T) FocessQQ.MainPlugin.getProperties().getOrDefault(key, t);
    }

    /**
     * Remove the property
     *
     * @param key the FocessQQ framework properties' key
     */
    public static void remove(final String key) {
        FocessQQ.MainPlugin.getProperties().remove(key);
    }
}
