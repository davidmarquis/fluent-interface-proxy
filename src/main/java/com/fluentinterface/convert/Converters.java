package com.fluentinterface.convert;

import java.text.DateFormat;
import java.text.ParseException;
import java.util.Date;

public class Converters {
    public static Conversions.Convert<String, Date> stringToDate = (value, target, converter) -> {
        try {
            return DateFormat.getDateInstance(DateFormat.SHORT).parse(value);
        } catch (ParseException e) {
            throw new IllegalArgumentException(String.format("Invalid date string %s", value), e);
        }
    };

    @SuppressWarnings("unchecked")
    public static Conversions.Convert<String, Enum> stringToEnum() {
        return (value, target, converter) -> Enum.valueOf(target, value);
    }
}
