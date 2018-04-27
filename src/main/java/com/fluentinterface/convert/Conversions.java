package com.fluentinterface.convert;

import javax.lang.model.type.NullType;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.*;
import java.util.*;

import static com.fluentinterface.convert.PathMatcher.match;
import static com.fluentinterface.convert.TypeMatcher.*;
import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toList;

public class Conversions {
    private final LinkedList<ConverterEntry> converters = new LinkedList<>();

    private Conversions(List<ConverterEntry> converters) {
        this.converters.addAll(converters);
    }

    public static Conversions empty() {
        return new Conversions(emptyList());
    }

    public static Conversions defaults() {
        return empty().addDefaultConverters();
    }

    public static Conversions of(Conversions... conversions) {
        return new Conversions(Arrays.stream(conversions)
                                     .map(c -> c.converters)
                                     .flatMap(Collection::stream)
                                     .collect(toList()));
    }

    public <S, T> Conversions add(TypeMatcher<S> source, TypeMatcher<T> destination, Convert<S, T> converter) {
        return add(match(source, destination), converter);
    }

    public <S, T> Conversions add(PathMatcher<S, T> matcher, Convert<S, T> converter) {
        converters.add(new ConverterEntry(matcher, converter));
        return this;
    }

    public Conversions addDefaultConverters() {
        add(nulls(), is(boolean.class), (v, t, c) -> false);
        add(nulls(), is(char.class), (v, t, c) -> (char) 0);
        add(nulls(), is(byte.class), (v, t, c) -> (byte) 0);
        add(nulls(), is(short.class), (v, t, c) -> (short) 0);
        add(nulls(), is(int.class), (v, t, c) -> 0);
        add(nulls(), is(long.class), (v, t, c) -> 0L);
        add(nulls(), is(float.class), (v, t, c) -> 0f);
        add(nulls(), is(double.class), (v, t, c) -> 0d);
        add(nulls(), any(), (v, t, c) -> null);

        add(is(String.class), is(Boolean.class, boolean.class), (v, t, c) -> Boolean.parseBoolean(v));
        add(is(String.class), is(Character.class, char.class), (v, t, c) -> v.length() > 0 ? v.charAt(0) : 0);
        add(is(String.class), is(Short.class, short.class), (v, t, c) -> Short.parseShort(v));
        add(is(String.class), is(Byte.class, byte.class), (v, t, c) -> Byte.parseByte(v));
        add(is(String.class), is(Integer.class, int.class), (v, t, c) -> Integer.parseInt(v));
        add(is(String.class), is(Long.class, long.class), (v, t, c) -> Long.parseLong(v));
        add(is(String.class), is(Float.class, float.class), (v, t, c) -> Float.parseFloat(v));
        add(is(String.class), is(Double.class, double.class), (v, t, c) -> Double.parseDouble(v));
        add(is(String.class), is(BigDecimal.class), (v, t, c) -> BigDecimal.valueOf(Double.parseDouble(v)));
        add(is(String.class), is(BigInteger.class), (v, t, c) -> BigInteger.valueOf(Long.parseLong(v)));

        add(is(String.class), is(Date.class), Converters.stringToDate);
        add(is(String.class), is(LocalDate.class), (v, t, c) -> LocalDate.parse(v));
        add(is(String.class), is(LocalTime.class), (v, t, c) -> LocalTime.parse(v));
        add(is(String.class), is(LocalDateTime.class), (v, t, c) -> LocalDateTime.parse(v));
        add(is(String.class), is(ZonedDateTime.class), (v, t, c) -> ZonedDateTime.parse(v));
        add(is(String.class), is(Instant.class), (v, t, c) -> Instant.parse(v));
        add(is(String.class), isInstanceOf(Enum.class), Converters.stringToEnum());

        add(any(), is(String.class), (v, t, c) -> v.toString());

        return this;
    }

    Optional<Convert> find(Type source, Type target) {
        final Type sourceType = source != null ? source : NullType.class;
        final Type targetType = target;

        return converters.stream()
                         .filter(c -> c.matcher.matches(sourceType, targetType))
                         .findFirst()
                         .map(c -> c.converter);
    }

    @FunctionalInterface
    public interface Convert<S, T> {
        T convert(S source, Class<T> target, Converter converter);
    }

    private static class ConverterEntry {
        private final PathMatcher matcher;
        private final Convert converter;

        private ConverterEntry(PathMatcher matcher, Convert converter) {
            this.matcher = matcher;
            this.converter = converter;
        }
    }
}
