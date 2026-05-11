package tech.thatgravyboat.repolib.v2.expl.value;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Function;

public record MutableArray(
        List<Value> entries,
        KeyValue.Mutable prototype
) implements Array.Mutable, KeyValue.Mutable.Forwarding {
    public static MutableArray create(List<Value> entries, Struct prototype) {
        return new MutableArray(entries, Array.createPrototype(entries, true, prototype));
    }
    public static MutableArray create(Function<List<Value>, Struct> prototype) {
        List<Value> entries = new ArrayList<>();
        return new MutableArray(entries, Array.createPrototype(entries, true, prototype.apply(entries)));
    }
    public static MutableArray create(Struct prototype) {
        List<Value> entries = new ArrayList<>();
        return new MutableArray(entries, Array.createPrototype(entries, true, prototype));
    }

    public static MutableArray create(List<Value> entries) {
        return new MutableArray(entries, Array.createPrototype(entries, true, ImmutableStruct.EMPTY));
    }

    public static MutableArray create() {
        return create(new ArrayList<>());
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
    public Array toImmutableArray() {
        return new ImmutableArray(new ArrayList<>(entries), prototype.toImmutable());
    }

    @Override
    public KeyValue.Mutable delegate() {
        return prototype;
    }

    @Override
    public @NotNull Iterator<Value> iterator() {
        return this.entries.iterator();
    }
}
