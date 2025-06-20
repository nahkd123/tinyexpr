package io.github.nahkd123.tinyexpr.impl;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;

import io.github.nahkd123.tinyexpr.Value;

public record MethodValue(MethodHandle handle, MethodType signature) implements Value {
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

    @FunctionalInterface
    public static interface D2DFunction {
        double apply(double a);
    }

    @FunctionalInterface
    public static interface DD2DFunction {
        double apply(double a, double b);
    }

    @FunctionalInterface
    public static interface DDD2DFunction {
        double apply(double a, double b, double c);
    }

    public static MethodValue of(D2DFunction f) {
        try {
            MethodType signature = MethodType.methodType(double.class, double.class);
            MethodHandle handle = MethodHandles.publicLookup().findVirtual(D2DFunction.class, "apply", signature);
            return new MethodValue(handle.bindTo(f), signature);
        } catch (Throwable t) {
            throw new RuntimeException(t);
        }
    }

    public static MethodValue of(DD2DFunction f) {
        try {
            MethodType signature = MethodType.methodType(double.class, double.class, double.class);
            MethodHandle handle = MethodHandles.publicLookup().findVirtual(DD2DFunction.class, "apply", signature);
            return new MethodValue(handle.bindTo(f), signature);
        } catch (Throwable t) {
            throw new RuntimeException(t);
        }
    }

    public static MethodValue of(DDD2DFunction f) {
        try {
            MethodType signature = MethodType.methodType(double.class, double.class, double.class, double.class);
            MethodHandle handle = MethodHandles.publicLookup().findVirtual(DDD2DFunction.class, "apply", signature);
            return new MethodValue(handle.bindTo(f), signature);
        } catch (Throwable t) {
            throw new RuntimeException(t);
        }
    }
}