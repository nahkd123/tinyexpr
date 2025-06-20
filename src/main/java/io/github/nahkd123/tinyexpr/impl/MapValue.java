package io.github.nahkd123.tinyexpr.impl;

import java.util.Map;
import java.util.function.Function;

import io.github.nahkd123.tinyexpr.Value;

public record MapValue(Map<String, Value> map) implements Value, Function<String, Value> {
    // Namespaces
    public static final MapValue MATH = new MapValue(Map.ofEntries(
        Map.entry("pi", new DoubleValue(Math.PI)),
        Map.entry("pow", MethodValue.of(Math::pow)),
        Map.entry("sqrt", MethodValue.of(Math::sqrt)),
        Map.entry("log2", MethodValue.of(Math::log)),
        Map.entry("log10", MethodValue.of(Math::log10)),
        Map.entry("min", MethodValue.of(Math::min)),
        Map.entry("max", MethodValue.of(Math::max)),
        Map.entry("sin", MethodValue.of(Math::sin)),
        Map.entry("cos", MethodValue.of(Math::cos)),
        Map.entry("tan", MethodValue.of(Math::tan)),
        Map.entry("asin", MethodValue.of(Math::asin)),
        Map.entry("acos", MethodValue.of(Math::acos)),
        Map.entry("atan", MethodValue.of(Math::atan)),
        Map.entry("atan2", MethodValue.of(Math::atan2)),
        Map.entry("clamp", MethodValue.of(Math::clamp))));

    @Override
    public Value get(String name) {
        Value existing = map.get(name);
        return existing != null ? existing : Value.super.get(name);
    }

    @Override
    public Value apply(String t) {
        return get(t);
    }

    @Override
    public Object unwrap() {
        return map;
    }

    @Override
    public final String toString() {
        return "[Map(%d)]".formatted(map.size());
    }
}
