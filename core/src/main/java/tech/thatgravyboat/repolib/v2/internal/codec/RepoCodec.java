package tech.thatgravyboat.repolib.v2.internal.codec;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import tech.thatgravyboat.repolib.v2.internal.ThrowingFunction;
import tech.thatgravyboat.repolib.v2.internal.codec.struct.Functions.Function1;
import tech.thatgravyboat.repolib.v2.internal.codec.struct.fields.StructDispatchFieldRepoCodec;
import tech.thatgravyboat.repolib.v2.internal.codec.struct.fields.StructFieldRepoCodec;
import tech.thatgravyboat.repolib.v2.internal.codec.struct.fields.StructNormalFieldRepoCodec;
import tech.thatgravyboat.repolib.v2.internal.codec.struct.fields.StructOptionalFieldRepoCodec;
import tech.thatgravyboat.repolib.v2.internal.utils.JsonUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;

public interface RepoCodec<Type> {
    RepoCodec<String> STRING = primitive(
            "string", JsonPrimitive::isString, JsonPrimitive::getAsString, JsonPrimitive::new
    );
    RepoCodec<Boolean> BOOLEAN = primitive(
            "boolean", JsonPrimitive::isBoolean, JsonPrimitive::getAsBoolean, JsonPrimitive::new
    );
    RepoCodec<Number> NUMBER = primitive(
            "number", JsonPrimitive::isNumber, JsonPrimitive::getAsNumber, JsonPrimitive::new
    );
    RepoCodec<Byte> BYTE = primitive(
            "byte", JsonPrimitive::isNumber, JsonPrimitive::getAsByte, JsonPrimitive::new
    );
    RepoCodec<Short> SHORT = primitive(
            "short", JsonPrimitive::isNumber, JsonPrimitive::getAsShort, JsonPrimitive::new
    );
    RepoCodec<Integer> INTEGER = primitive(
            "integer", JsonPrimitive::isNumber, JsonPrimitive::getAsInt, JsonPrimitive::new
    );
    RepoCodec<Long> LONG = primitive(
            "long", JsonPrimitive::isNumber, JsonPrimitive::getAsLong, JsonPrimitive::new
    );
    RepoCodec<Float> FLOAT = primitive(
            "float", JsonPrimitive::isNumber, JsonPrimitive::getAsFloat, JsonPrimitive::new
    );
    RepoCodec<Double> DOUBLE = primitive(
            "double", JsonPrimitive::isNumber, JsonPrimitive::getAsDouble, JsonPrimitive::new
    );

    RepoCodec<JsonElement> JSON = new RepoCodec<>() {
        @Override
        public CodecResult<JsonElement> decode(JsonElement element) {
            return CodecResult.success(element);
        }

        @Override
        public CodecResult<JsonElement> encode(JsonElement value) {
            return CodecResult.success(value);
        }
    };

    static <K, V> RepoCodec<Map<K, V>> Map(RepoCodec<K> keyCodec, RepoCodec<V> valueCodec) {
        return RepoCodecFunctions.map(keyCodec, valueCodec, Map::copyOf);
    }

    static <T> RepoCodec<List<T>> List(RepoCodec<T> elementCodec) {
        return RepoCodecFunctions.collection(elementCodec, List::copyOf);
    }

    static <T> RepoCodec<Set<T>> Set(RepoCodec<T> elementCodec) {
        return RepoCodecFunctions.collection(elementCodec, Set::copyOf);
    }

    private static <T> RepoCodec<T> primitive(
            String expectedType,
            Predicate<JsonPrimitive> validator,
            Function<JsonPrimitive, T> decoder,
            Function<T, JsonPrimitive> encoder
    ) {
        return new RepoCodec<>() {
            @Override
            public CodecResult<T> decode(JsonElement element) {
                if (element instanceof JsonPrimitive primitive && validator.test(primitive)) {
                    return CodecResult.success(decoder.apply(primitive));
                }
                return CodecResult.failure("Expected '" + expectedType + "' but got '" + JsonUtils.type(element) + "'");
            }

            @Override
            public CodecResult<JsonElement> encode(T value) {
                return CodecResult.success(encoder.apply(value));
            }
        };
    }

    CodecResult<Type> decode(JsonElement element);

    CodecResult<JsonElement> encode(Type value);

    default RepoCodec<List<Type>> listOf() {
        return List(this);
    }

    default RepoCodec<Set<Type>> setOf() {
        return Set(this);
    }

    default RepoCodec<Type> orElse(RepoCodec<Type> other) {
        return RepoCodecFunctions.orElse(this, List.of(other));
    }

    default StructFieldRepoCodec<Type> fieldOf(String field) {
        return new StructNormalFieldRepoCodec<>(field, this);
    }

    default StructFieldRepoCodec<Optional<Type>> optionalFieldOf(String field) {
        return new StructOptionalFieldRepoCodec<>(field, this);
    }

    default <Value> StructFieldRepoCodec<Value> dispatch(
            String key,
            Function1<Type, CodecResult<StructFieldRepoCodec<Value>>> to,
            Function1<Value, CodecResult<Type>> from
    ) {
        return new StructDispatchFieldRepoCodec<>(this.fieldOf(key), to, from);
    }

    default RepoCodec<Type> notNull() {
        return new RepoCodec<>() {
            @Override
            public CodecResult<Type> decode(JsonElement element) {
                return RepoCodec.this.decode(element).map(Objects::requireNonNull);
            }

            @Override
            public CodecResult<JsonElement> encode(Type value) {
                return RepoCodec.this.encode(value).map(Objects::requireNonNull);
            }
        };
    }

    default <Target> RepoCodec<Target> map(ThrowingFunction<Type, Target> to, ThrowingFunction<Target, Type> from) {
        return new RepoCodec<>() {
            @Override
            public CodecResult<Target> decode(JsonElement element) {
                return RepoCodec.this.decode(element).map(to);
            }

            @Override
            public CodecResult<JsonElement> encode(Target value) {
                try {
                    return RepoCodec.this.encode(from.apply(value));
                } catch (Exception e) {
                    return CodecResult.failure(e.getMessage());
                }
            }
        };
    }

    default <Target> RepoCodec<Target> flatMap(
            ThrowingFunction<Type, CodecResult<Target>> to,
            ThrowingFunction<Target, CodecResult<Type>> from
    ) {
        return new RepoCodec<>() {
            @Override
            public CodecResult<Target> decode(JsonElement element) {
                return RepoCodec.this.decode(element).flatMap(to);
            }

            @Override
            public CodecResult<JsonElement> encode(Target value) {
                try {
                    return from.apply(value).flatMap(RepoCodec.this::encode);
                } catch (Exception e) {
                    return CodecResult.failure(e.getMessage());
                }
            }
        };
    }

    static <Key, Value> IdMapper<Key, Value> idMapper() {
        return new IdMapper<>();
    }

    class IdMapper<Key, Value> {
        private final Map<Key, Value> keyMap = new HashMap<>();
        private final Map<Value, Key> valueMap = new HashMap<>();

        public RepoCodec<Value> dispatch(RepoCodec<Key> keyCodec) {
            return keyCodec.map(keyMap::get, valueMap::get).notNull();
        }

        private <T> void ensureNull(T current, T other) {
            if (current != null) throw new UnsupportedOperationException("Duplicate entry, current = '" + current + "', new = '" + other + "'");
        }

        public void put(Key key, Value value) {
            ensureNull(keyMap.put(key, value), value);
            ensureNull(valueMap.put(value, key), key);
        }
    }
}
