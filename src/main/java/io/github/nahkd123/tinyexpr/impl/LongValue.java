package io.github.nahkd123.tinyexpr.impl;

import io.github.nahkd123.tinyexpr.BinaryOp;
import io.github.nahkd123.tinyexpr.UnaryOp;
import io.github.nahkd123.tinyexpr.Value;

public record LongValue(long value) implements Value {
    @Override
    public Value op(BinaryOp op, Value another) {
        if (another instanceof LongValue o) return switch (op) {
        case ADD -> new LongValue(value + o.value);
        case SUBTRACT -> new LongValue(value - o.value);
        case MULTIPLY -> new LongValue(value * o.value);
        case DIVIDE -> new LongValue(value / o.value);
        case AND -> new LongValue(value & o.value);
        case OR -> new LongValue(value | o.value);
        case XOR -> new LongValue(value ^ o.value);
        case SHIFT_LEFT -> new LongValue(value << o.value);
        case SHIFT_RIGHT -> new LongValue(value >> o.value);
        case EQUALS -> new LongValue(value == o.value ? 1 : 0);
        case NOT_EQUALS -> new LongValue(value != o.value ? 1 : 0);
        case LESS_THAN -> new LongValue(value < o.value ? 1 : 0);
        case LESS_THAN_OR_EQUALS -> new LongValue(value <= o.value ? 1 : 0);
        case GREATER_THAN -> new LongValue(value > o.value ? 1 : 0);
        case GREATER_THAN_OR_EQUALS -> new LongValue(value >= o.value ? 1 : 0);
        default -> Value.super.op(op, another);
        };

        if (another instanceof DoubleValue o) return new DoubleValue(value).op(op, o);
        if (another instanceof StringValue o) return new StringValue(Long.toString(value) + o.value());
        return Value.super.op(op, another);
    }

    @Override
    public Value op(UnaryOp op) {
        return switch (op) {
        case INVERT -> new LongValue(~value);
        case NEGATE -> new LongValue(-value);
        case NOT -> new LongValue(value != 0 ? 0 : 1);
        default -> Value.super.op(op);
        };
    }

    @Override
    public Object unwrap() {
        return value;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T unwrapAs(Class<T> type) {
        if (type == byte.class) return (T) (Byte) (byte) value;
        if (type == short.class) return (T) (Short) (short) value;
        if (type == int.class) return (T) (Integer) (int) value;
        if (type == long.class) return (T) (Long) value;
        if (type == float.class) return (T) (Float) (float) value;
        if (type == double.class) return (T) (Double) (double) value;
        if (type == Number.class) return (T) (Number) value;
        if (type == boolean.class) return (T) (Boolean) (value != 0);
        return Value.super.unwrapAs(type);
    }

    @Override
    public final String toString() {
        return Long.toString(value);
    }
}