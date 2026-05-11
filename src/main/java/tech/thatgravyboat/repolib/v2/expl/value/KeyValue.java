package tech.thatgravyboat.repolib.v2.expl.value;

import java.util.Map;

non-sealed public interface KeyValue extends Value, Iterable<Map.Entry<String, Value>> {
    Value get(String field);

    Mutable toMutable();

    boolean contains(String field);

    interface Forwarding extends KeyValue {
        KeyValue delegate();

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

    interface Mutable extends KeyValue {
        interface Forwarding extends KeyValue.Forwarding, KeyValue.Mutable {

            @Override
            KeyValue.Mutable delegate();

            @Override
            default KeyValue toImmutable() {
                return delegate().toImmutable();
            }

            @Override
            default KeyValue toFullyImmutable() {
                return delegate().toFullyImmutable();
            }

            @Override
            default void set(String field, Value value) {
                delegate().set(field, value);
            }
        }

        void set(String field, Value value);

        KeyValue toImmutable();

        KeyValue toFullyImmutable();
    }
}
