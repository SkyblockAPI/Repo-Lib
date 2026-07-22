package tech.thatgravyboat.repolib.v2.expl.value;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.function.Function;

public record MutableArrayValue(
        List<Value> entries,
        KeyValue.Mutable prototype
) implements ArrayValue.Mutable, KeyValue.Mutable.Forwarding {
    public static MutableArrayValue create(List<Value> entries, StructValue prototype) {
        return new MutableArrayValue(entries, ArrayValue.createPrototype(entries, true, prototype));
    }
    public static MutableArrayValue create(Function<List<Value>, StructValue> prototype) {
        List<Value> entries = new ArrayList<>();
        return new MutableArrayValue(entries, ArrayValue.createPrototype(entries, true, prototype.apply(entries)));
    }
    public static MutableArrayValue create(StructValue prototype) {
        List<Value> entries = new ArrayList<>();
        return new MutableArrayValue(entries, ArrayValue.createPrototype(entries, true, prototype));
    }

    public static MutableArrayValue create(List<Value> entries) {
        return new MutableArrayValue(new ArrayList<>(entries), ArrayValue.createPrototype(entries, true, ImmutableStructValue.EMPTY));
    }

    public static MutableArrayValue create() {
        return create(new ArrayList<>());
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
    public void set(int index, Value value) {
        while (entries.size() < index) {
            entries.add(NIL);
        }
        entries.set(index, value);
    }

    @Override
    public void add(Value value) {
        entries.add(value);
    }

    @Override
    public ArrayValue toImmutableArray() {
        return new ImmutableArrayValue(new ArrayList<>(entries), prototype.toImmutable());
    }

    @Override
    public KeyValue.Mutable delegate() {
        return prototype;
    }

    @Override
    public @NotNull Iterator<Value> iterator() {
        return this.entries.iterator();
    }

    @Override
    public @NotNull String toString() {
        return entries.toString();
    }

    @Override
    public boolean isEmpty() {
        return entries.isEmpty();
    }
}
