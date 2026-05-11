package tech.thatgravyboat.repolib.v2.expl.expression;

import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.stream.Collectors;

public record Call(Expression lhs, List<Expression> args) implements Expression {

    @Override
    public @NotNull String toString() {
        return lhs + "(" + args.stream().map(Expression::toString).collect(Collectors.joining(", ")) + ")";
    }
}
