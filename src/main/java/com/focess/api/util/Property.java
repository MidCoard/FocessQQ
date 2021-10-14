package com.focess.api.util;

import com.focess.Main;

/**
 * Store some default properties of this framework.
 */
public class Property {

    /**
     * Indicate whether is
     * todo
     * @param key
     * @return
     */
    public static boolean hasKey(String key) {
        return Main.MainPlugin.getProperties().containsKey(key);
    }

    /**
     *
     * @param key
     * @param value
     */
    public static void put(String key, Object value) {
        Main.MainPlugin.getProperties().put(key, value);
    }

    /**
     *
     * @param key
     * @param <T>
     * @throws ClassCastException if
     * @return
     */
    public static <T> T get(String key) {
        return (T) Main.MainPlugin.getProperties().get(key);
    }
}
