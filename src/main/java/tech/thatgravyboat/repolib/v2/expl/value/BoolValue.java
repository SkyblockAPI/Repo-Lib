package tech.thatgravyboat.repolib.v2.expl.value;

import org.jetbrains.annotations.NotNull;

public final class BoolValue implements Value {
    public static final Value TRUE = new BoolValue(true);
    public static final Value FALSE = new BoolValue(false);
    private final boolean value;

    private BoolValue(boolean value) {this.value = value;}

    public static Value wrap(boolean value) {
        return value ? TRUE : FALSE;
    }

    @Override
    public @NotNull String toString() {
        return Boolean.toString(value);
    }

    @Override
    public int compareTo(@NotNull Value value) {
        if (value instanceof BoolValue boolValue) {
            return Boolean.compare(this.value, boolValue.value);
        }
        return 0;
    }

    @Override
    public String type() {
        return "boolean";
    }

    public boolean value() {return value;}


    @Override
    public int hashCode() {
        return Boolean.hashCode(value);
    }

}
