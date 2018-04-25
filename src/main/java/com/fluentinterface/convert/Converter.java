package com.fluentinterface.convert;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Optional;
import java.util.function.BiFunction;

import static com.fluentinterface.convert.Conversions.conversions;
import static com.fluentinterface.convert.TypeMatcher.*;

public class Converter {

    private static final Conversions defaultConversions =
            conversions().add(nulls(), is(boolean.class), (v, c) -> false)
                         .add(nulls(), is(char.class), (v, c) -> (char) 0)
                         .add(nulls(), is(byte.class), (v, c) -> (byte) 0)
                         .add(nulls(), is(short.class), (v, c) -> (short) 0)
                         .add(nulls(), is(int.class), (v, c) -> 0)
                         .add(nulls(), is(long.class), (v, c) -> 0L)
                         .add(nulls(), is(float.class), (v, c) -> 0f)
                         .add(nulls(), is(double.class), (v, c) -> 0d)
                         .add(nulls(), any(), (v, c) -> null)

                         .add(is(String.class), is(Boolean.class, boolean.class), (v, c) -> Boolean.parseBoolean(v))
                         .add(is(String.class), is(Character.class, char.class), (v, c) -> v.length() > 0 ? v.charAt(0) : 0)
                         .add(is(String.class), is(Short.class, short.class), (v, c) -> Short.parseShort(v))
                         .add(is(String.class), is(Byte.class, byte.class), (v, c) -> Byte.parseByte(v))
                         .add(is(String.class), is(Integer.class, int.class), (v, c) -> Integer.parseInt(v))
                         .add(is(String.class), is(Long.class, long.class), (v, c) -> Long.parseLong(v))
                         .add(is(String.class), is(Float.class, float.class), (v, c) -> Float.parseFloat(v))
                         .add(is(String.class), is(Double.class, double.class), (v, c) -> Double.parseDouble(v))
                         .add(is(String.class), is(BigDecimal.class), (v, c) -> BigDecimal.valueOf(Double.parseDouble(v)))
                         .add(is(String.class), is(BigInteger.class), (v, c) -> BigInteger.valueOf(Long.parseLong(v)))

                         .add(any(), is(String.class), (v, c) -> v.toString());

    private final Conversions conversions;

    public Converter(Conversions conversions) {
        this.conversions = Conversions.of(conversions, defaultConversions);
    }

    public Converter() {
        this.conversions = defaultConversions;
    }

    @SuppressWarnings("unchecked")
    public <S, T> T convert(S source, Class<T> targetType) {
        if (targetType.isInstance(source)) {
            return (T) source;
        }

        Class<?> sourceType = Optional.ofNullable(source)
                                      .map(s -> s.getClass())
                                      .orElse(null);

        return (T) conversions.find(sourceType, targetType)
                              .map(c -> c.convert(source, this))
                              .orElse(source);
    }

    public interface Convert<S, T> extends BiFunction<S, Converter, T> {}
}
