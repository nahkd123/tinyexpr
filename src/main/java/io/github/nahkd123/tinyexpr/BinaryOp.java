package io.github.nahkd123.tinyexpr;

import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

public enum BinaryOp {
    ADD(1),
    SUBTRACT(1),
    MULTIPLY(2),
    DIVIDE(2),
    SHIFT_LEFT(3),
    SHIFT_RIGHT(3),
    AND(3),
    OR(3),
    XOR(3),
    EQUALS(0),
    NOT_EQUALS(0),
    LESS_THAN(0),
    LESS_THAN_OR_EQUALS(0),
    GREATER_THAN(0),
    GREATER_THAN_OR_EQUALS(0),;

    private int priority;

    private BinaryOp(int priority) {
        this.priority = priority;
    }

    public int getPriority() { return priority; }

    public static final BinaryOp[][] SORTED = {
        { SHIFT_LEFT, SHIFT_RIGHT, AND, OR, XOR },
        { MULTIPLY, DIVIDE },
        { ADD, SUBTRACT },
        { EQUALS, NOT_EQUALS, LESS_THAN, LESS_THAN_OR_EQUALS, GREATER_THAN, GREATER_THAN_OR_EQUALS }
    };

    public static final List<Set<BinaryOp>> SORTED_COLLECTIONS = Stream.of(SORTED)
        .map(arr -> Set.of(arr))
        .toList();
}
