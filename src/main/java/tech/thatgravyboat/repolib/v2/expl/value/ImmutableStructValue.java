package tech.thatgravyboat.repolib.v2.expl.value;

import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

public record ImmutableStructValue(Map<String, Value> fields) implements StructValue {

    public static final ImmutableStructValue EMPTY = new ImmutableStructValue(Map.of());

    @Override
    public Value get(String field) {
        if (field.equals("this")) return this;
        return fields.getOrDefault(field, NIL);
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
    public Set<String> keySet() {
        return fields.keySet();
    }

    @Override
    public Map<String, KeyValue> sourceMap() {
        if (fields.isEmpty()) return Map.of();
        Map<String, KeyValue> result = new HashMap<>(fields.size() * 2);
        for (String key : keySet()) {
            result.put(key, this);
        }
        return result;
    }

    @Override
    public @NotNull Iterator<Map.Entry<String, Value>> iterator() {
        return fields.entrySet().iterator();
    }

    @Override
    public boolean isEmpty() {
        return fields.isEmpty();
    }

    @Override
    public MutableStruct toMutableStruct() {
        return new MutableStructValue(new HashMap<>(fields));
    }
}
