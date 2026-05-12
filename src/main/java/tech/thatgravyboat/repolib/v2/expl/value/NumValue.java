package tech.thatgravyboat.repolib.v2.expl.value;

import org.jetbrains.annotations.NotNull;

public record NumValue(double value) implements Value {
    public NumValue(boolean value) {
        this(value ? 1.0 : 0.0);
    }

    @Override
    public @NotNull String toString() {
        return String.valueOf(value);
    }
}
