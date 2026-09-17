package tech.thatgravyboat.repolib.v2.expl.value;

import org.jetbrains.annotations.NotNull;

import java.util.*;

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
