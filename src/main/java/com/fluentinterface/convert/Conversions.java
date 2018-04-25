package com.fluentinterface.convert;

import javax.lang.model.type.NullType;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static java.util.Collections.emptyList;

public class Conversions {
    private final LinkedList<TypeConverter> converters = new LinkedList<>();

    private Conversions(List<TypeConverter> converters) {
        this.converters.addAll(converters);
    }

    public static Conversions conversions() {
        return new Conversions(emptyList());
    }

    public static Conversions of(Conversions... conversions) {
        return new Conversions(Arrays.stream(conversions)
                                     .flatMap(c -> c.converters.stream())
                                     .collect(Collectors.toList()));
    }

    public <S, T> Conversions add(TypeMatcher<S> source, TypeMatcher<T> destination, Converter.Convert<S, T> converter) {
        converters.add(new TypeConverter<>(source, destination, converter));
        return this;
    }

    public Optional<TypeConverter> find(Type source, Type target) {
        return converters.stream()
                         .filter(c -> c.matches(source != null ? source : NullType.class, target))
                         .findFirst();
    }

    public static class TypeConverter<S, T> {
        private final TypeMatcher<S> source;
        private final TypeMatcher<T> destination;
        private final Converter.Convert<S, T> converter;

        private TypeConverter(TypeMatcher<S> source, TypeMatcher<T> destination, Converter.Convert<S, T> converter) {
            this.source = source;
            this.destination = destination;
            this.converter = converter;
        }

        boolean matches(Type source, Type target) {
            return this.source.matches(source) &&
                    this.destination.matches(target);
        }

        T convert(S source, Converter converter) {
            return this.converter.apply(source, converter);
        }
    }
}
