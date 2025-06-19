package io.github.nahkd123.tinyexpr;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodType;

/**
 * <p>
 * Represent a type of value that can be used when evaluating {@link Expr}.
 * Wrapping and unwrapping Java primitives (such as {@code int}) and
 * {@link String} can be done with {@link #wrap(Object)}. When getting the value
 * back as Java object, {@link #unwrapAs(Class)} is recommended.
 * </p>
 * 
 * @see #wrap(Object)
 * @see #unwrap()
 * @see #unwrapAs(Class)
 * @see JavaMethod
 * @see #get(String)
 * @see #get(int)
 * @see #call(Value[])
 * @see #op(UnaryOp)
 * @see #op(BinaryOp, Value)
 */
public interface Value {
    default Value get(String name) {
        throw new IllegalArgumentException("Property '%s' does not exists in %s".formatted(name, this));
    }

    default Value get(int index) {
        throw new IllegalArgumentException("Value %s is not indexable".formatted(this));
    }

    default Value call(Value[] params) {
        throw new IllegalArgumentException("Value %s is not callable".formatted(this));
    }

    default Value op(UnaryOp op) {
        throw new IllegalArgumentException("Value %s is not applicable for %s unary operator".formatted(this, op));
    }

    default Value op(BinaryOp op, Value another) {
        throw new IllegalArgumentException("Value %s is not applicable for %s %s".formatted(this, op, another));
    }

    Object unwrap();

    @SuppressWarnings("unchecked")
    default <T> T unwrapAs(Class<T> type) {
        Object o = unwrap();
        if (o == null) return null;
        if (type.isAssignableFrom(o.getClass())) return (T) o;
        throw new IllegalArgumentException("This %s is not %s".formatted(this, type));
    }

    /**
     * <p>
     * Wrap around Java object as {@link Value}.
     * </p>
     * 
     * @param value The Java object to wrap.
     * @return Wrapped Java object.
     */
    static Value wrap(Object value) {
        return switch (value) {
        case Byte v -> new JavaLong(v);
        case Short v -> new JavaLong(v);
        case Integer v -> new JavaLong(v);
        case Long v -> new JavaLong(v);
        case Float v -> new JavaDouble(v);
        case Double v -> new JavaDouble(v);
        case Number v -> new JavaDouble(v.doubleValue());
        case String v -> new JavaString(v);
        case null -> new JavaNull();
        default -> throw new IllegalArgumentException("Unable to wrap %s as ExprValue".formatted(value));
        };
    }

    record JavaLong(long value) implements Value {
        @Override
        public Value op(BinaryOp op, Value another) {
            if (another instanceof JavaLong o) return switch (op) {
            case ADD -> new JavaLong(value + o.value);
            case SUBTRACT -> new JavaLong(value - o.value);
            case MULTIPLY -> new JavaLong(value * o.value);
            case DIVIDE -> new JavaLong(value / o.value);
            case AND -> new JavaLong(value & o.value);
            case OR -> new JavaLong(value | o.value);
            case XOR -> new JavaLong(value ^ o.value);
            case SHIFT_LEFT -> new JavaLong(value << o.value);
            case SHIFT_RIGHT -> new JavaLong(value >> o.value);
            case EQUALS -> new JavaLong(value == o.value ? 1 : 0);
            case NOT_EQUALS -> new JavaLong(value != o.value ? 1 : 0);
            case LESS_THAN -> new JavaLong(value < o.value ? 1 : 0);
            case LESS_THAN_OR_EQUALS -> new JavaLong(value <= o.value ? 1 : 0);
            case GREATER_THAN -> new JavaLong(value > o.value ? 1 : 0);
            case GREATER_THAN_OR_EQUALS -> new JavaLong(value >= o.value ? 1 : 0);
            default -> Value.super.op(op, another);
            };

            if (another instanceof JavaDouble o) return new JavaDouble(value).op(op, o);
            if (another instanceof JavaString o) return new JavaString(Long.toString(value) + o.value);
            return Value.super.op(op, another);
        }

        @Override
        public Value op(UnaryOp op) {
            return switch (op) {
            case INVERT -> new JavaLong(~value);
            case NEGATE -> new JavaLong(-value);
            case NOT -> new JavaLong(value != 0 ? 0 : 1);
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

    record JavaDouble(double value) implements Value {
        @Override
        public Value op(BinaryOp op, Value another) {
            if (another instanceof JavaDouble o) return switch (op) {
            case ADD -> new JavaDouble(value + o.value);
            case SUBTRACT -> new JavaDouble(value - o.value);
            case MULTIPLY -> new JavaDouble(value * o.value);
            case DIVIDE -> new JavaDouble(value / o.value);
            case EQUALS -> new JavaLong(value == o.value ? 1 : 0);
            case NOT_EQUALS -> new JavaLong(value != o.value ? 1 : 0);
            case LESS_THAN -> new JavaLong(value < o.value ? 1 : 0);
            case LESS_THAN_OR_EQUALS -> new JavaLong(value <= o.value ? 1 : 0);
            case GREATER_THAN -> new JavaLong(value > o.value ? 1 : 0);
            case GREATER_THAN_OR_EQUALS -> new JavaLong(value >= o.value ? 1 : 0);
            default -> Value.super.op(op, another);
            };

            if (another instanceof JavaLong o) return op(op, new JavaDouble(o.value));
            if (another instanceof JavaString o) return new JavaString(Double.toString(value) + o.value);
            return Value.super.op(op, another);
        }

        @Override
        public Value op(UnaryOp op) {
            return switch (op) {
            case NEGATE -> new JavaDouble(-value);
            case NOT -> new JavaLong(value != 0 ? 0 : 1);
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
            if (type == long.class) return (T) (Long) (long) value;
            if (type == float.class) return (T) (Float) (float) value;
            if (type == double.class) return (T) (Double) value;
            if (type == Number.class) return (T) (Number) value;
            if (type == boolean.class) return (T) (Boolean) (value != 0);
            return Value.super.unwrapAs(type);
        }

        @Override
        public final String toString() {
            return Double.toString(value);
        }
    }

    record JavaString(String value) implements Value {
        @Override
        public Value op(BinaryOp op, Value another) {
            String a = another instanceof JavaString s ? s.value : another.toString();

            return switch (op) {
            case ADD -> new JavaString(value + a);
            case EQUALS -> new JavaLong(value.equals(a) ? 1 : 0);
            default -> Value.super.op(op, another);
            };
        }

        @Override
        public Value get(String name) {
            return switch (name) {
            case "len", "length" -> new JavaLong(value.length());
            case "upper", "uppercase" -> new JavaString(value.toUpperCase());
            case "lower", "lowercase" -> new JavaString(value.toLowerCase());
            default -> Value.super.get(name);
            };
        }

        @Override
        public Value get(int index) {
            if (index < 0 || index >= value.length()) throw new IndexOutOfBoundsException(index);
            return new JavaString(String.valueOf(value.charAt(index)));
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

    record JavaMethod(MethodHandle handle, MethodType signature) implements Value {
        @Override
        public Value call(Value[] params) {
            if (params.length != signature.parameterCount())
                throw new IllegalArgumentException("Expecting %d parameters, but found %d"
                    .formatted(signature.parameterCount(), params.length));

            Object[] values = new Object[params.length];
            for (int i = 0; i < values.length; i++) values[i] = params[i].unwrapAs(signature.parameterType(i));

            try {
                return Value.wrap(handle.invokeWithArguments(values));
            } catch (Throwable e) {
                throw new RuntimeException("Invocation failed", e);
            }
        }

        @Override
        public Object unwrap() {
            return handle;
        }

        @Override
        public final String toString() {
            return "[Method(%s)]".formatted(signature);
        }
    }

    record JavaNull() implements Value {
        @Override
        public Object unwrap() {
            return null;
        }

        @Override
        public final String toString() {
            return "[Null]";
        }
    }
}
