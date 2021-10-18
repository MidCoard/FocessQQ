package com.focess.api.util;

import java.io.Serializable;
import java.util.Map;
import java.util.Set;

/**
 * This is an util class to define basic access to the data.
 */
public interface SectionMap extends Serializable {

    /**
     * Store the key-value pair
     *
     * @param key the key of the key-value pair
     * @param value the value of the key-value pair
     */
    default void set(String key, Object value) {
        this.getValues().put(key,value);
    }

    /**
     * Get the value of the key-value pair
     * @param key the key of the key-value pair
     * @param <T> the desired type
     * @return the desired value
     * @throws ClassCastException if the desired T type is not equal to its original type
     */
    default <T> T get(String key) {
        return (T) this.getValues().get(key);
    }

    /**
     * Indicate there is a key-value pair named key
     *
     * @param key the key of the key-value pair
     * @return true there is a key-value pair named key, false otherwise
     */
    default boolean contains(String key) {
        return this.getValues().containsKey(key);
    }

    /**
     * Remove the key-value pair named key
     *
     * @param key the key of the key-value pair
     */
    default void remove(String key) {
        this.getValues().remove(key);
    }

    /**
     * Create the section named key
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
    Map<String,Object> getValues();

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
     * @param key the key of the Section
     * @return the section named key
     * @throws UnsupportedOperationException if there is no section named key
     */
    SectionMap getSection(String key);
}
