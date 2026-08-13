package tech.thatgravyboat.repolib.v2.expl.expression;

import org.jetbrains.annotations.NotNull;

import java.util.Locale;

public record StatementExpression(Op op) implements Expression {
    @Override
    public @NotNull String toString() {
        return op.name().toLowerCase(Locale.ROOT);
    }

    public enum Op {
        RETURN, BREAK, CONTINUE
    }
}
