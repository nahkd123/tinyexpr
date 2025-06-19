package io.github.nahkd123.tinyexpr;

import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * <p>
 * Represent an expression.
 * </p>
 * 
 * @see #eval(Function)
 */
public sealed interface Expr {
    /**
     * <p>
     * Evaluate the expression and return the value from the evaluation.
     * </p>
     * 
     * @param vars A function that returns {@link Value} for variable with provided
     *             name. Depending on expression, this may or may not be optional.
     * @return A value.
     */
    Value eval(Function<String, Value> vars);

    record Const(Value value) implements Expr {
        @Override
        public Value eval(Function<String, Value> vars) {
            return value;
        }

        @Override
        public final String toString() {
            return value.toString();
        }
    }

    record Variable(String name) implements Expr {
        @Override
        public Value eval(Function<String, Value> vars) {
            if (vars == null) throw new IllegalArgumentException("No access to current variables");
            return vars.apply(name);
        }

        @Override
        public final String toString() {
            return name;
        }
    }

    record Unary(UnaryOp op, Expr expr) implements Expr {
        @Override
        public Value eval(Function<String, Value> vars) {
            return expr.eval(vars).op(op);
        }

        @Override
        public final String toString() {
            return "%s%s".formatted(switch (op) {
            case INVERT -> "~";
            case NEGATE -> "-";
            case NOT -> "!";
            default -> op.toString();
            }, expr);
        }
    }

    record Binary(BinaryOp op, Expr a, Expr b) implements Expr {
        @Override
        public Value eval(Function<String, Value> vars) {
            return a.eval(vars).op(op, b.eval(vars));
        }

        @Override
        public final String toString() {
            return "(%s %s %s)".formatted(a, switch (op) {
            case ADD -> "+";
            case SUBTRACT -> "-";
            case MULTIPLY -> "*";
            case DIVIDE -> "/";
            case AND -> "&";
            case OR -> "|";
            case XOR -> "^";
            case EQUALS -> "==";
            case NOT_EQUALS -> "!=";
            case LESS_THAN -> "<";
            case LESS_THAN_OR_EQUALS -> "<=";
            case GREATER_THAN -> ">";
            case GREATER_THAN_OR_EQUALS -> ">=";
            default -> op.toString();
            }, b);
        }
    }

    record Index(Expr expr, Expr index) implements Expr {
        @Override
        public Value eval(Function<String, Value> vars) {
            return expr.eval(vars).get(index.eval(vars).unwrapAs(int.class));
        }

        @Override
        public final String toString() {
            return "%s[%s]".formatted(expr, index);
        }
    }

    record Property(Expr expr, String name) implements Expr {
        @Override
        public Value eval(Function<String, Value> vars) {
            return expr.eval(vars).get(name);
        }

        @Override
        public final String toString() {
            return "%s.%s".formatted(expr, name);
        }
    }

    record Call(Expr expr, Expr[] params) implements Expr {
        @Override
        public Value eval(Function<String, Value> vars) {
            Value[] inputs = new Value[params.length];
            for (int i = 0; i < inputs.length; i++) inputs[i] = params[i].eval(vars);
            return expr.eval(vars).call(inputs);
        }

        @Override
        public final String toString() {
            return "%s(%s)".formatted(expr, Stream.of(params).map(Expr::toString).collect(Collectors.joining(", ")));
        }
    }

    record Ternary(Expr test, Expr ifTrue, Expr ifFalse) implements Expr {
        @Override
        public Value eval(Function<String, Value> vars) {
            return test.eval(vars).unwrapAs(boolean.class) ? ifTrue.eval(vars) : ifFalse.eval(vars);
        }

        @Override
        public final String toString() {
            return "(%s ? %s : %s)".formatted(test, ifTrue, ifFalse);
        }
    }
}
