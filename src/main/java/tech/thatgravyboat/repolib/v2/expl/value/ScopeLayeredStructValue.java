package tech.thatgravyboat.repolib.v2.expl.value;

import java.util.*;

import org.jetbrains.annotations.NotNull;

public record ScopeLayeredStructValue(StructValue base, StructValue.MutableStruct overlay) implements StructValue.MutableStruct {

    @Override
    public void set(String field, Value value) {
        overlay.set(field, value);
    }

    @Override
    public ImmutableStructValue toImmutable() {
        var map = new HashMap<String, Value>();

        base.forEach((entry) -> map.put(entry.getKey(), entry.getValue()));
        overlay.forEach((entry) -> map.put(entry.getKey(), entry.getValue()));

        return new ImmutableStructValue(map);
    }

    @Override
    public KeyValue toFullyImmutable() {
        var map = new HashMap<String, Value>();
        for (var maps : List.of(base, overlay)) {
            for (var entry : maps) {
                if (entry instanceof Mutable mutable) {
                    map.put(entry.getKey(), mutable.toFullyImmutable());
                } else {
                    map.put(entry.getKey(), entry.getValue());
                }
            }
        }
        return new ImmutableStructValue(Map.copyOf(map));
    }

    @Override
    public boolean contains(String field) {
        if (base.isEmpty()) {
            if (overlay.isEmpty()) return false;
            return overlay.contains(field);
        }
        if (overlay.isEmpty()) {
            return base.contains(field);
        }
        return base.contains(field) || overlay.contains(field) ;
    }

    @Override
    public Value get(String field) {
        if (field.equals("this")) {
            return this;
        }

        if (!overlay.contains(field)) {
            return base.get(field);
        }
        return overlay.get(field);
    }

    @Override
    public boolean isEmpty() {
        return overlay.isEmpty() && base.isEmpty();
    }

    @Override
    public MutableStruct toMutableStruct() {
        return new ScopeLayeredStructValue(base.toMutableStruct(), overlay.toMutableStruct());
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
