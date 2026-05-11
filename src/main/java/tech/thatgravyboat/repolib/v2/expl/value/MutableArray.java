package tech.thatgravyboat.repolib.v2.expl.value;

import java.util.ArrayList;
import java.util.List;

public record MutableArray(
        List<Value> entries,
        KeyValue.Mutable prototype
) implements Array.Mutable, KeyValue.Mutable.Forwarding {
    public static MutableArray create(List<Value> entries, Struct prototype) {
        return new MutableArray(entries, Array.createPrototype(entries, true, prototype));
    }
    public static MutableArray create(List<Value> entries) {
        return new MutableArray(entries, Array.createPrototype(entries, true, ImmutableStruct.EMPTY));
    }

    @Override
    public Value get(int index) {
        if ((index < 0 && -index > entries.size()) || (index >= entries.size())) {
            return Value.NIL;
        }

        return entries.get(Math.floorMod(index, entries.size()));
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
}
