package tech.thatgravyboat.repolib.v2;

import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;

public sealed interface Value {

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

    record Num(double value) implements Value {
        public Num(boolean value) {
            this(value ? 1.0 : 0.0);
        }

        @Override
        public @NotNull String toString() {
            return String.valueOf(value);
        }
    }

    record Struct(Map<String, Value> fields) implements KeyValue.Mutable {
        @Override
        public Value get(String field) {
            return fields.getOrDefault(field, new Nil());
        }

        @Override
        public void set(String field, Value value) {
            fields.put(field, value);
        }

        @Override
        public String toString() {
            return fields.toString();
        }
    }

    @FunctionalInterface
    non-sealed interface Function extends Value {

        Value apply(List<Value> args);
    }

    non-sealed interface KeyValue extends Value {
        Value get(String field);

        interface Mutable extends KeyValue {
            void set(String field, Value value);
        }
    }

}
