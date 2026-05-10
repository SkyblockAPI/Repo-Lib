package tech.thatgravyboat.repolib.v2;

import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public sealed interface Value {

    Value NIL = new Nil();

    record Nil() implements Value {
        @Override
        public @NotNull String toString() {
            return "undefined";
        }
    }

    record Str(String value) implements Value {
        @Override
        public @NotNull String toString() {
            return '"' + value + '"';
        }
    }

    record Bool(boolean value) implements Value {
        @Override
        public @NotNull String toString() {
            return Boolean.toString(value);
        }
    }

    record Num(double value) implements Value {
        public Num(boolean value) {
            this(value ? 1.0 : 0.0);
        }

        @Override
        public @NotNull String toString() {
            return String.valueOf(value);
        }
    }

    record MutableStruct(Map<String, Value> fields) implements KeyValue.Mutable {
        public MutableStruct() {
            this(new HashMap<>());
        }

        @Override
        public Value get(String field) {
            return fields.computeIfAbsent(field, (ignored) -> new MutableStruct());
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
        public KeyValue toImmutable() {
            return new ImmutableStruct(Map.copyOf(fields));
        }

        @Override
        public Mutable toMutable() {
            return new MutableStruct(new HashMap<>(fields));
        }

        @Override
        public @NotNull Iterator<Map.Entry<String, Value>> iterator() {
            return fields.entrySet().iterator();
        }
    }

    record ImmutableStruct(Map<String, Value> fields) implements KeyValue {

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
        public @NotNull Iterator<Map.Entry<String, Value>> iterator() {
            return fields.entrySet().iterator();
        }
    }

    @FunctionalInterface
    non-sealed interface Function extends Value {

        Value apply(List<Value> args);
    }

    non-sealed interface KeyValue extends Value, Iterable<Map.Entry<String, Value>> {
        Value get(String field);

        KeyValue.Mutable toMutable();

        interface Mutable extends KeyValue {
            void set(String field, Value value);

            KeyValue toImmutable();
        }
    }

}
