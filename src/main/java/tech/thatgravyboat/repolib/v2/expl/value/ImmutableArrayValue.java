package tech.thatgravyboat.repolib.v2.expl.value;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public record ImmutableArrayValue(
        List<Value> entries,
        KeyValue prototype
) implements ArrayValue, KeyValue.Forwarding {
    public ImmutableArrayValue(List<Value> entries, StructValue prototype) {
        this(entries, ArrayValue.createPrototype(entries, false, prototype));
    }

    @Override
    public Value get(int index) {
        return ArrayValue.get(entries, index);
    }

    @Override
    public KeyValue.Mutable toMutableArray() {
        return new MutableArrayValue(new ArrayList<>(entries), prototype.toMutable());
    }

    @Override
    public KeyValue delegate() {
        return prototype;
    }

    @Override
    public @NotNull String toString() {
        return entries.toString();
    }

    @Override
    public @NotNull Iterator<Value> iterator() {
        return entries.iterator();
    }

    @Override
    public boolean isEmpty() {
        return entries.isEmpty();
    }
}
