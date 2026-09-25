package tech.thatgravyboat.repolib.v2.expl.value;

import java.util.Map;
import java.util.Set;

non-sealed public interface KeyValue extends Value {
    Value get(String field);

    Mutable toMutable();

    boolean contains(String field);

    boolean isEmpty();

    Set<String> keySet();
    Map<String, KeyValue> sourceMap();

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

        @Override
        default boolean isEmpty() {
            return delegate().isEmpty();
        }

        @Override
        default Set<String> keySet() {
            return delegate().keySet();
        }

        @Override
        default Map<String, KeyValue> sourceMap() {
            return delegate().sourceMap();
        }
    }

    interface Mutable extends KeyValue {
        void set(String field, Value value);

        KeyValue toImmutable();

        KeyValue toFullyImmutable();

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
    }

    @Override
    default String type() {
        return "object";
    }

    @Override
    default boolean asBooleanConversion() {
        return !this.isEmpty();
    }

    @Override
    default KeyValue asKeyValue() {
        return this;
    }

    @Override
    default Value containsValue(String value) {
        return BoolValue.wrap(this.contains(value));
    }
}
