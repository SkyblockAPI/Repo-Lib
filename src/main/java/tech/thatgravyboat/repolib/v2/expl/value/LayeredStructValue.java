package tech.thatgravyboat.repolib.v2.expl.value;

import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

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
        if (overlay.isEmpty()) {
            if (base.isEmpty()) return false;
            return base.contains(field);
        }
        if (base.isEmpty()) {
            return overlay.contains(field);
        }
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
    public Set<String> keySet() {
        Set<String> baseKeySet = base.keySet();
        Set<String> overlayKeySet = overlay.keySet();

        if (baseKeySet.isEmpty()) return overlayKeySet;
        if (overlayKeySet.isEmpty()) return baseKeySet;

        Set<String> set = new HashSet<>(baseKeySet.size() + overlayKeySet.size());
        set.addAll(baseKeySet);
        set.addAll(overlayKeySet);
        return set;
    }

    @Override
    public @NotNull Iterator<Map.Entry<String, Value>> iterator() {
        var keys = keySet();

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
