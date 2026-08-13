package tech.thatgravyboat.repolib.v2.expl.value;

import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

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
