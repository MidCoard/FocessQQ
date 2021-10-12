package com.focess.util;

import java.io.Serializable;

/**
 * This is an Easy Util Class to store two Instances.
 *
 * @param <K> the type of first element of the Pair
 * @param <V> the type of second element of the Pair
 */
public class Pair<K, V> implements Serializable {

    /**
     * The first element
     */
    private final K key;

    /**
     * The second element
     */
    private final V value;

    public Pair(K key, V value) {
        this.key = key;
        this.value = value;
    }

    public static <K, V> Pair<K, V> of(K key, V value) {
        return new Pair<>(key, value);
    }

    public K getKey() {
        return key;
    }

    public V getValue() {
        return value;
    }

    public K getLeft() {
        return this.key;
    }

    public V getRight() {
        return this.value;
    }

    public K getFirst() {
        return this.key;
    }

    public V getSecond() {
        return this.value;
    }

    @Override
    public String toString() {
        return "(" + key + ',' + value + ')';
    }
}
