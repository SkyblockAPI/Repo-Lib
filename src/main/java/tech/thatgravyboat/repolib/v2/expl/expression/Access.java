package tech.thatgravyboat.repolib.v2.expl.expression;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public record Access(@Nullable Expression lhs, String field) implements Expression {

    @Override
    public @NotNull String toString() {
        return lhs + "." + field;
    }
}
