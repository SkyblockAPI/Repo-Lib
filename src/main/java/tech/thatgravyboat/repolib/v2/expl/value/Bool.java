package tech.thatgravyboat.repolib.v2.expl.value;

import org.jetbrains.annotations.NotNull;

public record Bool(boolean value) implements Value {

    @Override
    public @NotNull String toString() {
        return Boolean.toString(value);
    }
}
