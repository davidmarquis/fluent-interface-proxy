package com.fluentinterface.utils;

import com.fluentinterface.ReflectionBuilder;
import com.fluentinterface.builder.Builder;
import org.junit.Test;

import java.util.Map;

import static com.fluentinterface.utils.MapBuilder.mappingOf;
import static com.fluentinterface.utils.MapBuilder.map;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.*;

public class MapBuilderTest {
    
    @Test
    public void testBuildTypedMap() throws Exception {

        Map<String, Integer> built =
                mappingOf("key1", 2)
                        .and("key2", 5)
                        .build();

        assertThat("map size", built.size(), is(2));
        assertThat(built.get("key1"), is(2));
        assertThat(built.get("key2"), is(5));
    }
    
    @Test
    public void testBuildUntypedMap() throws Exception {

        Map<Object, Object> built =
                map()
                        .with("key1", 5)
                        .with("key2", "value")
                        .build();

        assertThat("map size", built.size(), is(2));
        assertThat(built.get("key1"), is(5));
        assertThat(built.get("key2"), is("value"));
    }

    @Test
    public void testUsageWithinOtherBuilder() throws Exception {

        BeanBuilder aBean = ReflectionBuilder
                .implementationFor(BeanBuilder.class)
                .usingFieldsDirectly()
                .create();

        Bean built = aBean
                .withDetails(mappingOf("key1", 2))
                .build();

        assertThat("map size", built.details.size(), is(1));
        assertThat(built.details.get("key1"), is(2));
    }

    public static class Bean {
        Map<String, Integer> details;
    }

    private interface BeanBuilder extends Builder<Bean> {
        BeanBuilder withDetails(MapBuilder<String, Integer> details);
    }
}