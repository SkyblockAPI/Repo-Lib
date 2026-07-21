package tech.thatgravyboat.repolib.v2.expl.value;

import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public record MutableStructValue(Map<String, Value> fields) implements StructValue, KeyValue.Mutable {
    public MutableStructValue() {
        this(new HashMap<>());
    }
    public MutableStructValue(StructValue value) {
        this(asMap(value));
    }

    private static HashMap<String, Value> asMap(StructValue value) {
        return switch (value) {
            case MutableStructValue(Map<String, Value> fields) -> new HashMap<>(fields);
            case ImmutableStructValue(Map<String, Value> fields) -> new HashMap<>(fields);
            default -> {
                var map = new HashMap<String, Value>();
                value.forEach(entry -> map.put(entry.getKey(), entry.getValue()));
                yield map;
            }
        };
    }

    @Override
    public Value get(String field) {
        if (field.equals("this")) return this;
        return fields.computeIfAbsent(field, (ignored) -> new MutableStructValue());
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
        return new ImmutableStructValue(Map.copyOf(fields));
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
        return new ImmutableStructValue(Map.copyOf(map));
    }

    @Override
    public Mutable toMutable() {
        return new MutableStructValue(new HashMap<>(fields));
    }

    @Override
    public @NotNull Iterator<Map.Entry<String, Value>> iterator() {
        return fields.entrySet().iterator();
    }

    @Override
    public boolean isEmpty() {
        return fields.isEmpty();
    }
}
