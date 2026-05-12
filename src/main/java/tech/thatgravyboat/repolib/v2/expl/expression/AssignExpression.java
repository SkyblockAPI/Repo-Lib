package tech.thatgravyboat.repolib.v2.expl.expression;

import org.jetbrains.annotations.NotNull;

public record AssignExpression(AccessExpression lhs, Expression value) implements Expression {

    @Override
    public @NotNull String toString() {
        return lhs + " = " + value;
    }
}
