package com.fluentinterface.convert;

import org.junit.Test;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.*;
import java.util.Date;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

public class ConverterTest {

    private final Converter converter = new Converter(Conversions.defaults());

    @Test
    public void convertsNullsToPrimitives() {
        assertThat("null->boolean", converter.convert(null, boolean.class), is(false));
        assertThat("null->char", converter.convert(null, char.class), is((char) 0));
        assertThat("null->byte", converter.convert(null, byte.class), is((byte) 0));
        assertThat("null->short", converter.convert(null, short.class), is((short) 0));
        assertThat("null->int", converter.convert(null, int.class), is(0));
        assertThat("null->long", converter.convert(null, long.class), is((long) 0));
        assertThat("null->float", converter.convert(null, float.class), is(0f));
        assertThat("null->double", converter.convert(null, double.class), is(0d));
    }

    @Test
    public void convertsNullsToBoxedPrimitives() {
        assertThat("null->Boolean", converter.convert(null, Boolean.class), nullValue());
        assertThat("null->Character", converter.convert(null, Character.class), nullValue());
        assertThat("null->Byte", converter.convert(null, Byte.class), nullValue());
        assertThat("null->Short", converter.convert(null, Short.class), nullValue());
        assertThat("null->Integer", converter.convert(null, Integer.class), nullValue());
        assertThat("null->Long", converter.convert(null, Long.class), nullValue());
        assertThat("null->Float", converter.convert(null, Float.class), nullValue());
        assertThat("null->Double", converter.convert(null, Double.class), nullValue());
    }

    @Test
    public void convertsNullObjectsToNull() {
        assertThat("null->String", converter.convert(null, String.class), nullValue());
        assertThat("null->List", converter.convert(null, List.class), nullValue());
    }

    @Test
    public void convertsPrimitivesToString() {
        assertThat("Boolean->String", converter.convert(true, String.class), is("true"));
        assertThat("Character->String", converter.convert((char) 65, String.class), is("A"));
        assertThat("Byte->String", converter.convert((byte) 12, String.class), is("12"));
        assertThat("Short->String", converter.convert((short) 44, String.class), is("44"));
        assertThat("Integer->String", converter.convert(182, String.class), is("182"));
        assertThat("Long->String", converter.convert(1881L, String.class), is("1881"));
        assertThat("Float->String", converter.convert(661f, String.class), is("661.0"));
        assertThat("Double->String", converter.convert(817d, String.class), is("817.0"));
        assertThat("BigInteger->String", converter.convert(BigInteger.valueOf(1993881), String.class), is("1993881"));
        assertThat("BigDecimal->String", converter.convert(BigDecimal.valueOf(199.18), String.class), is("199.18"));
    }

    @Test
    public void convertsStringToPrimitives() {
        assertThat("String->Boolean", converter.convert("true", Boolean.class), is(true));
        assertThat("String->boolean", converter.convert("true", boolean.class), is(true));
        assertThat("String->Integer", converter.convert("123", Integer.class), is(123));
        assertThat("String->int", converter.convert("123", int.class), is(123));
        assertThat("String->Byte", converter.convert("54", Byte.class), is((byte) 54));
        assertThat("String->byte", converter.convert("54", byte.class), is((byte) 54));
        assertThat("String->Long", converter.convert("8000", Long.class), is(8000L));
        assertThat("String->long", converter.convert("8000", long.class), is(8000L));
        assertThat("String->Character", converter.convert("A", Character.class), is((char) 65));
        assertThat("String->char", converter.convert("A", char.class), is((char) 65));
        assertThat("String->Float", converter.convert("991.13", Float.class), is(991.13f));
        assertThat("String->float", converter.convert("991.13", float.class), is(991.13f));
        assertThat("String->Double", converter.convert("182.19", Double.class), is(182.19));
        assertThat("String->double", converter.convert("182.19", double.class), is(182.19));
        assertThat("String->BigInteger", converter.convert("182.19", BigDecimal.class), is(BigDecimal.valueOf(182.19)));
        assertThat("String->BigDecimal", converter.convert("8817774", BigInteger.class), is(BigInteger.valueOf(8817774)));
    }

    @Test
    public void convertsISODateStringToDates() {
        assertThat("String->Date", converter.convert("2018-08-12", Date.class),
                   is(Date.from(LocalDate.of(2018, 8, 12).atStartOfDay(ZoneId.systemDefault()).toInstant())));
        assertThat("String->LocalDate", converter.convert("2018-08-12", LocalDate.class),
                   is(LocalDate.of(2018, 8, 12)));
        assertThat("String->LocalTime", converter.convert("07:54:34", LocalTime.class),
                   is(LocalTime.of(7, 54, 34)));
        assertThat("String->LocalDateTime", converter.convert("2018-08-12T07:54:34", LocalDateTime.class),
                   is(LocalDateTime.of(2018, 8, 12, 7, 54, 34)));
        assertThat("String->ZonedDateTime", converter.convert("2018-08-12T07:54:34Z[UTC]", ZonedDateTime.class),
                   is(ZonedDateTime.of(LocalDateTime.of(2018, 8, 12, 7, 54, 34), ZoneId.of("UTC"))));
        assertThat("String->Instant", converter.convert("2018-08-12T07:54:34Z", Instant.class),
                   is(Instant.ofEpochSecond(1534060474)));
    }

    private enum Fruit {apple}

    @Test
    public void convertsStringToEnum() {
        assertThat("String->Enum", converter.convert("apple", Fruit.class), is(Fruit.apple));
    }

    @Test
    public void doesNotConvertWhenObjectIsInstanceOfTargetType() {
        Integer value = 123;
        assertThat("Integer->Number", converter.convert(value, Number.class), sameInstance(value));
    }

    @Test
    public void usesCustomConvertersFirst() {
        Converter converter = new Converter(
                Conversions.empty()
                           .add(TypeMatcher.is(Integer.class), TypeMatcher.is(String.class), (v, t, c) -> "custom")
                           .addDefaultConverters());

        assertThat("custom conversion", converter.convert(999, String.class), is("custom"));
    }
}