package tech.thatgravyboat.repolib.v2.expl.value;

import org.jetbrains.annotations.NotNull;

public record BoolValue(boolean value) implements Value {

    public static final Value TRUE = new BoolValue(true);
    public static final Value FALSE = new BoolValue(false);

    public static Value wrap(boolean value) {
        return value ? TRUE : FALSE;
    }

    @Override
    public @NotNull String toString() {
        return Boolean.toString(value);
    }

    @Override
    public int compareTo(@NotNull Value value) {
        if (value instanceof BoolValue(boolean literal)) {
            return Boolean.compare(this.value, literal);
        }
        return 0;
    }

    @Override
    public String type() {
        return "boolean";
    }
}
