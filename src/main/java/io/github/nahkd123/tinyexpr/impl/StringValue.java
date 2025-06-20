package io.github.nahkd123.tinyexpr.impl;

import io.github.nahkd123.tinyexpr.BinaryOp;
import io.github.nahkd123.tinyexpr.Value;

public record StringValue(String value) implements Value {
    @Override
    public Value op(BinaryOp op, Value another) {
        String a = another instanceof StringValue s ? s.value : another.toString();

        return switch (op) {
        case ADD -> new StringValue(value + a);
        case EQUALS -> new LongValue(value.equals(a) ? 1 : 0);
        default -> Value.super.op(op, another);
        };
    }

    @Override
    public Value get(String name) {
        return switch (name) {
        case "len", "length" -> new LongValue(value.length());
        case "upper", "uppercase" -> new StringValue(value.toUpperCase());
        case "lower", "lowercase" -> new StringValue(value.toLowerCase());
        default -> Value.super.get(name);
        };
    }

    @Override
    public Value get(int index) {
        if (index < 0 || index >= value.length()) throw new IndexOutOfBoundsException(index);
        return new StringValue(String.valueOf(value.charAt(index)));
    }

    @Override
    public Object unwrap() {
        return value;
    }

    @Override
    public final String toString() {
        return value;
    }
}