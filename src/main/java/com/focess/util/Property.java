package com.focess.util;

import com.focess.Main;

public class Property {

    public static boolean hasKey(String key) {
        return Main.MainPlugin.getProperties().containsKey(key);
    }

    public static void put(String key, Object value) {
        Main.MainPlugin.getProperties().put(key, value);
    }

    public static <T> T get(String key) {
        return (T) Main.MainPlugin.getProperties().get(key);
    }
}
