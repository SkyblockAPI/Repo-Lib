package tech.thatgravyboat.repolib.v2.expl.value;

import org.jetbrains.annotations.NotNull;

public record Bool(boolean value) implements Value {

    public static final Value TRUE = new Bool(true);
    public static final Value FALSE = new Bool(false);

    @Override
    public @NotNull String toString() {
        return Boolean.toString(value);
    }
}
