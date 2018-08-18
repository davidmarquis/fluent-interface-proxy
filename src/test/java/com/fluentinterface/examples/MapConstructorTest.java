package com.fluentinterface.examples;

import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static com.fluentinterface.examples.MapConstructorBuilder.aMapConstructor;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class MapConstructorTest {
    @Test
    public void instantiatesWithHashMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("key", "value");

        MapConstructor created = aMapConstructor().of(map).build();

        assertThat(created.map.get("key"), is("value"));
    }
}
