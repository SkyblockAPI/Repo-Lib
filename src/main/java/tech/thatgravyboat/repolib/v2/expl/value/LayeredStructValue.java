package tech.thatgravyboat.repolib.v2.expl.value;

import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;

public record LayeredStructValue(StructValue.MutableStruct base, KeyValue overlay) implements StructValue, KeyValue.Mutable {
    @Override
    public Value get(String field) {
        if (overlay.contains(field)) {
            return overlay.get(field);
        }

        return base.get(field);
    }

    @Override
    public StructValue.MutableStruct toMutableStruct() {
        return base.toMutableStruct();
    }

    @Override
    public boolean contains(String field) {
        return overlay.contains(field) || base.contains(field) ;
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

    @Override
    public boolean isEmpty() {
        return overlay.isEmpty() && base.isEmpty();
    }

    @Override
    public @NotNull Iterator<Map.Entry<String, Value>> iterator() {
        var keys = new HashSet<String>();
        if (overlay instanceof Iterable<?> iterable) {
            iterable.forEach(e -> {
                if (e instanceof Map.Entry entry) {
                    keys.add((String) entry.getKey());
                }
            });
        }
        base.forEach(e -> keys.add(e.getKey()));

        var parent = keys.iterator();

        return new Iterator<>() {
            @Override
            public boolean hasNext() {
                return parent.hasNext();
            }

            @Override
            public Map.Entry<String, Value> next() {
                String key = parent.next();
                return Map.entry(key, get(key));
            }
        };
    }
}
