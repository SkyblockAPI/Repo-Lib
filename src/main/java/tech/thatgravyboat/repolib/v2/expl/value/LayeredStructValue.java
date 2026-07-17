package tech.thatgravyboat.repolib.v2.expl.value;

import org.jetbrains.annotations.NotNull;

import java.util.Iterator;
import java.util.Map;

public record LayeredStructValue<Type extends StructValue & KeyValue.Mutable>(Type base, KeyValue overlay) implements StructValue, KeyValue.Mutable {
    @Override
    public @NotNull Iterator<Map.Entry<String, Value>> iterator() {
        return base.iterator();
    }

    @Override
    public Value get(String field) {
        if (overlay.contains(field)) {
            return overlay.get(field);
        }

        return base.get(field);
    }

    @Override
    public Mutable toMutable() {
        return base.toMutable();
    }

    @Override
    public boolean contains(String field) {
        return base.contains(field) || overlay.contains(field);
    }

    @Override
    public void set(String field, Value value) {
        base.set(field, value);
    }

    @Override
    public KeyValue toImmutable() {
        return base.toImmutable();
    }

    @Override
    public KeyValue toFullyImmutable() {
        return base.toFullyImmutable();
    }
}
