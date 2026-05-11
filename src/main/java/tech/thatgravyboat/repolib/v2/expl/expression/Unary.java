package tech.thatgravyboat.repolib.v2.expl.expression;

import org.jetbrains.annotations.NotNull;

public record Unary(Op op, Expression rhs) implements Expression {
    @Override
    public @NotNull String toString() {
        return switch (op) {
            case NEGATE -> "-" + rhs;
            case NOT -> "!" + rhs;
        };
    }

    public enum Op {
        NEGATE, NOT
    }
}
