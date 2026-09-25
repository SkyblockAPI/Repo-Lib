package tech.thatgravyboat.repolib.v2.expl.value;

import java.util.Objects;
import org.jetbrains.annotations.NotNull;

public record StrValue(String value) implements Value {
    @Override
    public @NotNull String toString() {
        return '"' + value + '"';
    }

    @Override
    public int compareTo(@NotNull Value other) {
        if (other instanceof StrValue(String c)) {
            return value.compareTo(c);
        }
        return 0;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof StrValue(String literal)) {
            return Objects.equals(this.value, literal);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return value.hashCode();
    }

    @Override
    public String type() {
        return "string";
    }

    @Override
    public String asString() {
        return this.value;
    }

    @Override
    public String asStringOrNull() {
        return this.value;
    }

    @Override
    public boolean asBooleanConversion() {
        return !this.value.isEmpty();
    }

    @Override
    public Value containsValue(String value) {
        return BoolValue.wrap(this.value.contains(value));
    }
}
