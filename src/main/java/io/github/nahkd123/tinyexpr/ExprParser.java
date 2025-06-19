package io.github.nahkd123.tinyexpr;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * <p>
 * This class provides parse static methods, as well as instance methods for
 * setting up expression parsing manually. The latter also enables overriding
 * the {@code valueOf} instance methods, allowing the usage of different
 * implementation of {@link Value} for number and string.
 * </p>
 * 
 * @see #parse(CharSequence)
 * @see #parse(CharSequence, int, int)
 * @see #beginParse()
 * @see #push(ExprToken)
 * @see #endParse()
 * @see #valueOf(io.github.nahkd123.tinyexpr.ExprToken.NumberLiteral)
 * @see #valueOf(io.github.nahkd123.tinyexpr.ExprToken.StringLiteral)
 */
public class ExprParser {
    private int mode = 0;
    private UnaryOp unary = null;
    private List<ParseGroup> stack = new ArrayList<>();

    private class ParseGroup {
        List<Expr> exprs = new ArrayList<>();
        List<BinaryOp> ops = new ArrayList<>();

        void push(Expr expression) {
            if (exprs.size() - ops.size() == 1) throw new IllegalStateException("Must push operator next");
            exprs.add(expression);
        }

        Expr popExpr() {
            if (exprs.size() == 0) throw new IllegalStateException("Group is empty");
            if (exprs.size() - ops.size() != 1) throw new IllegalStateException("The last element is an operator");
            return exprs.removeLast();
        }

        void push(String property) {
            if (exprs.size() - ops.size() == 0) throw new IllegalStateException("Must push expression next");
            Expr expr = new Expr.Property(exprs.removeLast(), property);
            exprs.add(expr);
        }

        void push(BinaryOp operator) {
            if (exprs.size() - ops.size() == 0) throw new IllegalStateException("Must push expression next");
            ops.add(operator);
        }

        Expr build() {
            if (exprs.size() == 0) throw new IllegalStateException("Group is empty");

            while (exprs.size() != 1) {
                for (Set<BinaryOp> set : BinaryOp.SORTED_COLLECTIONS) {
                    for (int i = 0; i < exprs.size() - 1; i++) {
                        BinaryOp op = ops.get(i);
                        if (!set.contains(op)) continue;

                        Expr left = exprs.remove(i);
                        Expr right = exprs.remove(i);
                        ops.remove(i);
                        exprs.add(i, new Expr.Binary(op, left, right));
                    }
                }
            }

            return exprs.getFirst();
        }
    }

    private final class RootGroup extends ParseGroup {
    }

    private final class SingleGroup extends ParseGroup {
    }

    private final class CallGroup extends ParseGroup {
        private Expr base;
        private List<Expr> params = new ArrayList<>();

        public CallGroup(Expr base) {
            this.base = base;
        }

        void nextParam() {
            params.add(super.build());
            exprs.clear();
            ops.clear();
        }

        @Override
        Expr build() {
            if (exprs.size() > 0) nextParam();
            return new Expr.Call(base, params.toArray(Expr[]::new));
        }
    }

    private final class IndexGroup extends ParseGroup {
        private Expr base;

        public IndexGroup(Expr base) {
            this.base = base;
        }

        @Override
        Expr build() {
            return new Expr.Index(base, super.build());
        }
    }

    private final class TernaryGroup extends ParseGroup {
        private Expr test, ifTrue;

        public TernaryGroup(Expr test) {
            this.test = test;
        }

        void switchToIfFalse() {
            if (ifTrue != null) throw new IllegalStateException("Already have truthy expression");
            ifTrue = super.build();
            exprs.clear();
            ops.clear();
        }

        @Override
        Expr build() {
            if (ifTrue == null) throw new IllegalStateException("Missing truthy expression");
            return new Expr.Ternary(test, ifTrue, super.build());
        }
    }

    /**
     * <p>
     * Begin parsing by setting up initial root group.
     * </p>
     */
    public void beginParse() {
        stack.add(new RootGroup());
    }

    /**
     * <p>
     * Finish parsing and build the root group into an expression.
     * </p>
     * 
     * @return A new expression built from root group.
     */
    public Expr endParse() {
        processTernary();
        return stack.removeLast().build();
    }

    /**
     * <p>
     * Push a new token to this parser. Requires calling {@link #beginParse()}
     * before pushing new token. The tokens are typically obtained from
     * {@link ExprToken#tokenize(CharSequence, int, int, java.util.function.Consumer)}.
     * </p>
     * 
     * @param token A new token to push.
     * @see #beginParse()
     * @see #endParse()
     */
    public void push(ExprToken token) {
        if (stack.size() == 0)
            throw new IllegalStateException("Expression stack is empty (have you called beginParse() yet?)");

        switch (mode) {
        case 0: // Expecting value, symbol or any in '+-~!()'
            switch (token) {
            case ExprToken.StringLiteral literal:
                stack.getLast().push(new Expr.Const(valueOf(literal)));
                mode = 1;
                break;
            case ExprToken.NumberLiteral literal:
                stack.getLast().push(new Expr.Const(valueOf(literal)));
                mode = 1;
                break;
            case ExprToken.Symbol symbol:
                stack.getLast().push(new Expr.Variable(symbol.name()));
                mode = 1;
                break;
            case ExprToken.Keyword.ADD:
                mode = 2;
                break;
            case ExprToken.Keyword.SUB:
            case ExprToken.Keyword.INVERT:
            case ExprToken.Keyword.NOT:
                unary = switch ((ExprToken.Keyword) token) {
                case SUB -> UnaryOp.NEGATE;
                case INVERT -> UnaryOp.INVERT;
                case NOT -> UnaryOp.NOT;
                default -> throw new IllegalArgumentException("Unexpected value: " + token);
                };
                mode = 2;
                break;
            case ExprToken.Keyword.GOPEN:
                stack.add(new SingleGroup());
                break;
            case ExprToken.Keyword.GCLOSE:
                processTernary();
                if (!(stack.getLast() instanceof CallGroup last))
                    throw new IllegalArgumentException("')' can only be used in function call context here");
                stack.removeLast();
                stack.getLast().push(last.build());
                mode = 1;
                break;
            default:
                throw new IllegalArgumentException("Expecting +-~!(), symbol or value but found %s".formatted(token));
            }
            break;
        case 1: // Expecting binary operator or any in '.,()[]?:'
            switch (token) {
            case ExprToken.Keyword.GOPEN:
                stack.add(new CallGroup(stack.getLast().popExpr()));
                mode = 0;
                break;
            case ExprToken.Keyword.GCLOSE: {
                processTernary();
                ParseGroup group = stack.getLast();
                if (!(group instanceof SingleGroup || group instanceof CallGroup))
                    throw new IllegalArgumentException("')' can only be used in function call or group context here");
                stack.removeLast();
                stack.getLast().push(group.build());
                break;
            }
            case ExprToken.Keyword.BOPEN:
                stack.add(new IndexGroup(stack.getLast().popExpr()));
                mode = 0;
                break;
            case ExprToken.Keyword.BCLOSE: {
                processTernary();
                if (!(stack.getLast() instanceof IndexGroup last))
                    throw new IllegalArgumentException("']' can only be used in index context here");
                stack.removeLast();
                stack.getLast().push(last.build());
                break;
            }
            case ExprToken.Keyword.NEXT:
                processTernary();
                if (!(stack.getLast() instanceof CallGroup last))
                    throw new IllegalArgumentException("',' can only be used in function call context here");
                last.nextParam();
                mode = 0;
                break;
            case ExprToken.Keyword.PROPERTY:
                mode = 3;
                break;
            case ExprToken.Keyword.TERNARY_TEST:
                stack.add(new TernaryGroup(stack.getLast().popExpr()));
                mode = 0;
                break;
            case ExprToken.Keyword.TERNARY_OR: {
                if (!(stack.getLast() instanceof TernaryGroup last))
                    throw new IllegalArgumentException("':' can only be used in ternary context here");
                last.switchToIfFalse();
                mode = 0;
                break;
            }
            case ExprToken.Keyword.ADD:
            case ExprToken.Keyword.SUB:
            case ExprToken.Keyword.MUL:
            case ExprToken.Keyword.DIV:
            case ExprToken.Keyword.SHL:
            case ExprToken.Keyword.SHR:
            case ExprToken.Keyword.AND:
            case ExprToken.Keyword.OR:
            case ExprToken.Keyword.XOR:
            case ExprToken.Keyword.EQ:
            case ExprToken.Keyword.NE:
            case ExprToken.Keyword.LT:
            case ExprToken.Keyword.LE:
            case ExprToken.Keyword.GT:
            case ExprToken.Keyword.GE:
                stack.getLast().push(switch ((ExprToken.Keyword) token) {
                case ADD -> BinaryOp.ADD;
                case SUB -> BinaryOp.SUBTRACT;
                case MUL -> BinaryOp.MULTIPLY;
                case DIV -> BinaryOp.DIVIDE;
                case SHL -> BinaryOp.SHIFT_LEFT;
                case SHR -> BinaryOp.SHIFT_RIGHT;
                case AND -> BinaryOp.AND;
                case OR -> BinaryOp.OR;
                case XOR -> BinaryOp.XOR;
                case EQ -> BinaryOp.EQUALS;
                case NE -> BinaryOp.NOT_EQUALS;
                case LT -> BinaryOp.LESS_THAN;
                case LE -> BinaryOp.LESS_THAN_OR_EQUALS;
                case GT -> BinaryOp.GREATER_THAN;
                case GE -> BinaryOp.GREATER_THAN_OR_EQUALS;
                default -> throw new IllegalArgumentException("Unexpected value: " + token);
                });
                mode = 0;
                break;
            default:
                throw new IllegalArgumentException("Expecting binaryop or any in '.,()[]?:' but found %s"
                    .formatted(token));
            }
            break;
        case 2: { // Expecting just value
            Expr expr = switch (token) {
            case ExprToken.StringLiteral literal -> new Expr.Const(valueOf(literal));
            case ExprToken.NumberLiteral literal -> new Expr.Const(valueOf(literal));
            default -> throw new IllegalArgumentException("Expecting value but found %s".formatted(token));
            };

            if (unary != null) {
                expr = new Expr.Unary(unary, expr);
                unary = null;
            }

            stack.getLast().push(expr);
            mode = 1;
            break;
        }
        case 3: // Expecting just name
            if (!(token instanceof ExprToken.Symbol symbol))
                throw new IllegalArgumentException("Expecting symbol but found %s".formatted(token));
            stack.getLast().push(symbol.name());
            mode = 1;
            break;
        default:
            throw new IllegalStateException("State not implemented: %s".formatted(mode));
        }
    }

    private void processTernary() {
        if (!(stack.getLast() instanceof TernaryGroup ternary)) return;
        stack.removeLast();
        stack.getLast().push(ternary.build());
    }

    /**
     * <p>
     * Override this to use custom implementation of {@link Value} for string
     * literals.
     * </p>
     * 
     * @param literal The token for string literal.
     * @return Runtime value.
     */
    protected Value valueOf(ExprToken.StringLiteral literal) {
        return new Value.JavaString(literal.value());
    }

    /**
     * <p>
     * Override this to use custom implementation of {@link Value} for number
     * literals.
     * </p>
     * 
     * @param literal The token for number literal.
     * @return Runtime value.
     */
    protected Value valueOf(ExprToken.NumberLiteral literal) {
        int base = switch (literal.type()) {
        case ExprToken.NumberLiteral.TYPE_OCTAL -> 8;
        case ExprToken.NumberLiteral.TYPE_HEXADECIMAL -> 16;
        default -> 10;
        };

        long intg = literal.intg().length() == 0 ? 0L : Long.parseUnsignedLong(literal.intg(), base);
        if (literal.frac().length() == 0) return new Value.JavaLong(intg);

        long frac = Long.parseUnsignedLong(literal.frac(), base);
        double fracMax = base == 10
            ? Math.pow(10, literal.frac().length())
            : 1 << (literal.frac().length() * (base == 16 ? 4 : 3));

        return new Value.JavaDouble(intg + frac / fracMax);
    }

    /**
     * <p>
     * Parse input into expression with input limit.
     * </p>
     * 
     * @param input The input sequence of characters, typically {@link String}.
     * @param start The start index.
     * @param end   The end index.
     * @return An expression parsed from given input and range.
     */
    public static Expr parse(CharSequence input, int start, int end) {
        ExprParser parser = new ExprParser();
        parser.beginParse();
        ExprToken.tokenize(input, start, end, parser::push);
        return parser.endParse();
    }

    /**
     * <p>
     * Parse input into expression ({@link Expr}).
     * </p>
     * 
     * @param input The input sequence of characters, typically {@link String}.
     * @return An expression parsed from given input.
     */
    public static Expr parse(CharSequence input) {
        return parse(input, 0, input.length());
    }
}
