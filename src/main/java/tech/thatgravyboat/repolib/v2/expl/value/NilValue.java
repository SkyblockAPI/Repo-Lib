package tech.thatgravyboat.repolib.v2.expl.value;

import org.jetbrains.annotations.NotNull;

public record NilValue() implements Value {
    @Override
    public @NotNull String toString() {
        return "undefined";
    }

    @Override
    public String type() {
        return "null";
    }
}
