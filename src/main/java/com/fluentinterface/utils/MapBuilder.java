package com.fluentinterface.utils;

import com.fluentinterface.builder.Builder;

import java.util.HashMap;
import java.util.Map;

/**
 * Utility builder to create a map with a fluent API. Because this builder implements the <pre>{@code Builder<>}</pre> interface, it can
 * be used directly in other builders as parameters, without the need to call `build()`.
 *
 * Direct usage:
 * <pre>{@code
 * mappingof("key", 2).and("other", 3).build();
 * }</pre>
 *
 * Usage within other builders:
 * <pre>{@code
 * class Person {
 *  public Map<String, Integer> details;
 * }
 *
 * interface PersonBuilder extends Builder<Person> {
 *      PersonBuilder withDetails(MapBuilder<String, Integer> details);
 * }
 *
 * aPerson().withDetails(mappingOf("playerNumber", 1929).and("attribute", 12993))
 * }</pre>
 *
 * @param <K> type for the map's keys
 * @param <V> type for the map's values
 */
public class MapBuilder<K, V> implements Builder<Map<K, V>> {

    private Map<K, V> built = new HashMap<>();

    private MapBuilder() {}

    private MapBuilder(K initialKey, V initialValue) {
        built.put(initialKey, initialValue);
    }

    public MapBuilder<K, V> and(K key, V value) {
        built.put(key, value);
        return this;
    }

    public MapBuilder<K, V> with(K key, V value) {
        built.put(key, value);
        return this;
    }

    public static <K, V> MapBuilder<K, V> mappingOf(K key, V value) {
        return new MapBuilder<>(key, value);
    }

    public static MapBuilder<Object, Object> map() {
        return new MapBuilder<>();
    }

    @Override
    public Map<K, V> build(Object... ignored) {
        return built;
    }
}
