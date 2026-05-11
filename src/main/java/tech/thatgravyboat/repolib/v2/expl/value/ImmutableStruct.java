package tech.thatgravyboat.repolib.v2.expl.value;

import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public record ImmutableStruct(Map<String, Value> fields) implements Struct {

    public static final ImmutableStruct EMPTY = new ImmutableStruct(Map.of());

    @Override
    public Value get(String field) {
        return fields.getOrDefault(field, NIL);
    }

    @Override
    public String toString() {
        return fields.toString();
    }

    @Override
    public MutableStruct toMutable() {
        return new MutableStruct(new HashMap<>(fields));
    }

    @Override
    public boolean contains(String field) {
        return fields.containsKey(field);
    }

    @Override
    public @NotNull Iterator<Map.Entry<String, Value>> iterator() {
        return fields.entrySet().iterator();
    }
}
