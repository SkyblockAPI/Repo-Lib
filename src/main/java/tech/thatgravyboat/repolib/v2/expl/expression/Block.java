package tech.thatgravyboat.repolib.v2.expl.expression;

import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.stream.Collectors;

public record Block(List<Expression> exprs) implements Expression {

    @Override
    public @NotNull String toString() {
        if (exprs.isEmpty()) {
            return "{}";
        }
        return "{" + exprs.stream().map(Expression::toString).collect(Collectors.joining("; ")) + "}";
    }
}
