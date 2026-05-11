package tech.thatgravyboat.repolib.v2.expl.value;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public record ImmutableArray(
        List<Value> entries,
        KeyValue prototype
) implements Array, KeyValue.Forwarding {
    public ImmutableArray(List<Value> entries, Struct prototype) {
        this(entries, Array.createPrototype(entries, false, prototype));
    }

    @Override
    public Value get(int index) {
        return Array.get(entries, index);
    }

    @Override
    public KeyValue.Mutable toMutableArray() {
        return new MutableArray(new ArrayList<>(entries), prototype.toMutable());
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
}
