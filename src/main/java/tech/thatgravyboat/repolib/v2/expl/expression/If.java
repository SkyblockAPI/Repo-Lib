package tech.thatgravyboat.repolib.v2.expl.expression;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public record If(Expression cond, Expression thenExpr, @Nullable Expression elseExpr) implements Expression {

    @Override
    public @NotNull String toString() {
        if (elseExpr == null) {
            return String.format("if (%s) %s", cond, thenExpr);
        }
        return String.format("if (%s) %s else %s", cond, thenExpr, elseExpr);
    }
}
