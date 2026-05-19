package tech.thatgravyboat.repolib.v2.expl.expression;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public record AccessExpression(@Nullable Expression lhs, Expression field) implements Expression {

    @Override
    public @NotNull String toString() {
        return lhs + "." + field;
    }
}
