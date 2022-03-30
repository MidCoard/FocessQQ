package top.focess.qq.api.util;

import java.io.Serializable;
import java.util.Map;
import java.util.Set;
import java.util.function.BiFunction;

/**
 * This is an util class to define basic access to the data.
 */
public interface SectionMap extends Serializable {

    /**
     * Store the key-value pair
     *
     * @param key   the key of the key-value pair
     * @param value the value of the key-value pair
     */
    default void set(final String key, final Object value) {
        this.getValues().put(key, value);
    }

    /**
     * Get the value of the key-value pair
     *
     * @param key the key of the key-value pair
     * @param <T> the value type
     * @return the value
     * @throws ClassCastException if the value is not the specified type
     */
    default <T> T get(final String key) {
        return (T) this.getValues().get(key);
    }

    /**
     * Indicate there is a key-value pair named key
     *
     * @param key the key of the key-value pair
     * @return true there is a key-value pair named key, false otherwise
     */
    default boolean contains(final String key) {
        return this.getValues().containsKey(key);
    }

    /**
     * Remove the key-value pair named key
     *
     * @param key the key of the key-value pair
     */
    default void remove(final String key) {
        this.getValues().remove(key);
    }

    /**
     * Create the section named key
     *
     * Note: if the section named key already exists, it will be replaced by a new section
     *
     * @param key the key of the Section
     * @return a section named key
     */
    SectionMap createSection(String key);

    /**
     * Get all the key-value pairs
     *
     * @return all the key-value pairs
     */
    Map<String, Object> getValues();

    /**
     * Get all the keys in set
     *
     * @return all the keys in set
     */
    default Set<String> keys() {
        return this.getValues().keySet();
    }

    /**
     * Get the section named key
     *
     * Note: if the section named key does not exist, it will be created
     *
     * @param key the key of the Section
     * @return the section named key
     * @throws UnsupportedOperationException if there is no section named key
     */
    SectionMap getSection(String key);

    /**
     * Indicate there is a section named key
     *
     * @param key the key of the Section
     * @return true there is a section named key, false otherwise
     */
    boolean containsSection(String key);

    /**
     * Get the value of the key-value pair
     *
     * @param key          the key of the key-value pair
     * @param defaultValue the default value
     * @param <T>          the value type
     * @return the value or defaultValue if there is no value
     */
    default <T> T getOrDefault(final String key, final T defaultValue) {
        return this.getValues().containsKey(key) ? this.get(key) : defaultValue;
    }

    /**
     * compute the value of the key-value pair
     *
     * @param key               the key of the key-value pair
     * @param remappingFunction the remapping function
     */
    default void compute(final String key, final BiFunction<? super String, ? super Object, ?> remappingFunction) {
        this.getValues().compute(key, remappingFunction);
    }
}
