package io.github.nahkd123.tinyexpr;

import io.github.nahkd123.tinyexpr.impl.DoubleValue;
import io.github.nahkd123.tinyexpr.impl.LongValue;
import io.github.nahkd123.tinyexpr.impl.MethodValue;
import io.github.nahkd123.tinyexpr.impl.NullValue;
import io.github.nahkd123.tinyexpr.impl.StringValue;

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
 * @see MethodValue
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

    default Object unwrap() {
        throw new IllegalArgumentException("Value %s cannot be unwrapped".formatted(this));
    }

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
        case Boolean v -> new LongValue(v ? 1 : 0);
        case Byte v -> new LongValue(v);
        case Short v -> new LongValue(v);
        case Integer v -> new LongValue(v);
        case Long v -> new LongValue(v);
        case Float v -> new DoubleValue(v);
        case Double v -> new DoubleValue(v);
        case Number v -> new DoubleValue(v.doubleValue());
        case String v -> new StringValue(v);
        case null -> NullValue.NULL;
        default -> throw new IllegalArgumentException("Unable to wrap %s as ExprValue".formatted(value));
        };
    }
}
