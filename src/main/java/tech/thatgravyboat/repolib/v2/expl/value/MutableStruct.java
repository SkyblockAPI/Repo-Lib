package tech.thatgravyboat.repolib.v2.expl.value;

import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public record MutableStruct(Map<String, Value> fields) implements Struct, KeyValue.Mutable {
    public MutableStruct() {
        this(new HashMap<>());
    }

    @Override
    public Value get(String field) {
        return fields.computeIfAbsent(field, (ignored) -> new tech.thatgravyboat.repolib.v2.expl.value.MutableStruct());
    }

    @Override
    public void set(String field, Value value) {
        fields.put(field, value);
    }

    @Override
    public String toString() {
        return fields.toString();
    }

    @Override
    public boolean contains(String field) {
        return fields.containsKey(field);
    }

    @Override
    public KeyValue toImmutable() {
        return new ImmutableStruct(Map.copyOf(fields));
    }

    @Override
    public KeyValue toFullyImmutable() {
        var map = new HashMap<String, Value>();
        for (var entry : fields.entrySet()) {
            if (entry instanceof Mutable mutable) {
                map.put(entry.getKey(), mutable.toFullyImmutable());
            } else {
                map.put(entry.getKey(), entry.getValue());
            }
        }
        return new ImmutableStruct(Map.copyOf(map));
    }

    @Override
    public Mutable toMutable() {
        return new tech.thatgravyboat.repolib.v2.expl.value.MutableStruct(new HashMap<>(fields));
    }

    @Override
    public @NotNull Iterator<Map.Entry<String, Value>> iterator() {
        return fields.entrySet().iterator();
    }
}
