package io.github.nahkd123.tinyexpr.impl;

import java.util.List;

import io.github.nahkd123.tinyexpr.Value;

public record ListValue(List<Value> list) implements Value {
    @Override
    public Value get(int index) {
        if (index < 0 || index >= list.size()) throw new IndexOutOfBoundsException(index);
        return list.get(index);
    }

    @Override
    public Value get(String name) {
        return switch (name) {
        case "len", "length" -> new LongValue(list.size());
        default -> Value.super.get(name);
        };
    }

    @Override
    public Object unwrap() {
        return list;
    }

    @Override
    public final String toString() {
        return "[List(%d)]".formatted(list.size());
    }
}
