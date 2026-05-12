package tech.thatgravyboat.repolib.v2.expl.value;

import org.jetbrains.annotations.NotNull;

public record BoolValue(boolean value) implements Value {

    public static final Value TRUE = new BoolValue(true);
    public static final Value FALSE = new BoolValue(false);

    @Override
    public @NotNull String toString() {
        return Boolean.toString(value);
    }
}
