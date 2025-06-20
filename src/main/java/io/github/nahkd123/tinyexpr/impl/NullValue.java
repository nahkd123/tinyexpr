package io.github.nahkd123.tinyexpr.impl;

import io.github.nahkd123.tinyexpr.Value;

public class NullValue implements Value {
    public static final NullValue NULL = new NullValue();

    private NullValue() {}

    @Override
    public Object unwrap() {
        return null;
    }

    @Override
    public final String toString() {
        return "[Null]";
    }
}