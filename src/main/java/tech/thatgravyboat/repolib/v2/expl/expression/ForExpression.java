package tech.thatgravyboat.repolib.v2.expl.expression;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public record ForExpression(@Nullable Expression init, @Nullable Expression cond, @Nullable Expression incr, Expression body) implements Expression {

    @Override
    public @NotNull String toString() {
        return "for (%s;%s;%s) %s".formatted(
                init == null ? "" : init,
                cond == null ? "" : cond,
                incr == null ? "" : incr,
                body
        );
    }
}
