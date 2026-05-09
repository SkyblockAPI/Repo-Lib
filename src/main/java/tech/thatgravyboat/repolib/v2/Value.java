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


    @FunctionalInterface
    non-sealed interface Function extends Value {

        Value apply(List<Value> args);

    }

    non-sealed interface Struct extends Value {
        static Struct holder(Map<String, Value> map) {
            return new MolangEvaluator.HolderImpl(Map.copyOf(map));
        }

        Value get(String field);

        interface Mutable extends Struct {
            void set(String field, Value value);
        }
    }

}
