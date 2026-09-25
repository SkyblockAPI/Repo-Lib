package tech.thatgravyboat.repolib.v2.expl.value;

import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import java.util.*;

import org.jetbrains.annotations.NotNull;

public record ScopeLayeredStructValue(StructValue base, StructValue.MutableStruct overlay) implements StructValue.MutableStruct {

    @Override
    public void set(String field, Value value) {
        if (base.contains(field) && !overlay.contains(field)) {
            if (base instanceof KeyValue.Mutable mutableBase) {
                mutableBase.set(field, value);
            }
        } else {
            overlay.set(field, value);
        }
    }

    @Override
    public ImmutableStructValue toImmutable() {
        var map = new HashMap<String, Value>(base.size() + overlay.size());

        base.forEach((entry) -> map.put(entry.getKey(), entry.getValue()));
        overlay.forEach((entry) -> map.put(entry.getKey(), entry.getValue()));

        return new ImmutableStructValue(map);
    }

    @Override
    public int size() {
        return this.base.size() + this.overlay.size();
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
        if (overlay.isEmpty()) {
            if (base.isEmpty()) return false;
            return base.contains(field);
        }
        if (base.isEmpty()) {
            return overlay.contains(field);
        }
        return base.contains(field) || overlay.contains(field) ;
    }

    @Override
    public Value get(String field) {
        if (field.equals("this")) {
            return this;
        }

        if (base.contains(field) && !overlay.contains(field)) {
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

        Set<String> set = new HashSet<>((baseKeySet.size() + overlayKeySet.size()) * 2);
        set.addAll(baseKeySet);
        set.addAll(overlayKeySet);
        return set;
    }

    @Override
    public Map<String, KeyValue> sourceMap() {
        Map<String, KeyValue> overlayMap = overlay.sourceMap();
        Map<String, KeyValue> baseMap = base.sourceMap();
        if (overlayMap.isEmpty()) return baseMap;
        if (baseMap.isEmpty()) return overlayMap;
        Map<String, KeyValue> result = new HashMap<>((overlayMap.size() + baseMap.size()) * 2);
        result.putAll(baseMap);
        result.putAll(overlayMap);
        return result;
    }

    @Override
    public @NotNull Iterator<Map.Entry<String, Value>> iterator() {
        var keys = sourceMap();

        var parent = keys.entrySet().iterator();

        return new Iterator<>() {
            @Override
            public boolean hasNext() {
                return parent.hasNext();
            }

            @Override
            public Map.Entry<String, Value> next() {
                Map.Entry<String, KeyValue> key = parent.next();
                return Map.entry(key.getKey(), key.getValue().get(key.getKey()));
            }
        };
    }
}
