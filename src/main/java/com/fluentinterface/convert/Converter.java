package com.fluentinterface.convert;

import java.util.Optional;

public class Converter {

    private final Conversions conversions;

    public Converter(Conversions conversions) {
        this.conversions = conversions;
    }

    public Converter() {
        this(Conversions.defaults());
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
                              .map(c -> c.convert(source, targetType, this))
                              .orElse(source);
    }
}
