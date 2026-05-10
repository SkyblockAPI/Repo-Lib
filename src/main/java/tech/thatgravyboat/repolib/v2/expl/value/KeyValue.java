package tech.thatgravyboat.repolib.v2.expl.value;

import java.util.Map;

non-sealed public interface KeyValue extends Value, Iterable<Map.Entry<String, Value>> {
    Value get(String field);

    Mutable toMutable();

    boolean contains(String field);

    interface Forwarding extends tech.thatgravyboat.repolib.v2.expl.value.KeyValue {
        tech.thatgravyboat.repolib.v2.expl.value.KeyValue delegate();

        @Override
        default Value get(String field) {
            return delegate().get(field);
        }

        @Override
        default Mutable toMutable() {
            return delegate().toMutable();
        }

        @Override
        default boolean contains(String field) {
            return delegate().contains(field);
        }
    }

    interface Mutable extends tech.thatgravyboat.repolib.v2.expl.value.KeyValue {
        void set(String field, Value value);

        tech.thatgravyboat.repolib.v2.expl.value.KeyValue toImmutable();

        tech.thatgravyboat.repolib.v2.expl.value.KeyValue toFullyImmutable();
    }
}
