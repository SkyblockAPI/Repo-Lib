package tech.thatgravyboat.repolib.v2.internal.codec;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public class RepoCodecFunctions {

    static <K, V, M extends Map<K, V>> RepoCodec<M> map(
            RepoCodec<K> keyCodec,
            RepoCodec<V> valueCodec,
            Function<Map<K, V>, M> factory
    ) {
        return new RepoCodec<>() {
            @Override
            public CodecResult<M> decode(JsonElement element) {
                if (element instanceof JsonObject object) {
                    var map = new LinkedHashMap<K, V>();
                    for (var entry : object.entrySet()) {
                        switch (keyCodec.decode(new JsonPrimitive(entry.getKey()))) {
                            case CodecResult.Success<K> key -> {
                                switch (valueCodec.decode(entry.getValue())) {
                                    case CodecResult.Success<V> value -> map.put(key.value(), value.value());
                                    case CodecResult.Failure<V> failure -> {
                                        return CodecResult.failure("Failed to decode value for key '" + entry.getKey() + "': " + failure.error());
                                    }
                                }
                            }
                            case CodecResult.Failure<K> failure -> {
                                return CodecResult.failure("Failed to decode key '" + entry.getKey() + "': " + failure.error());
                            }
                        }
                    }
                    return CodecResult.success(factory.apply(map));
                }
                return CodecResult.failure("Expected an object");
            }

            @Override
            public CodecResult<JsonElement> encode(M map) {
                var object = new JsonObject();

                for (var entry : map.entrySet()) {
                    switch (keyCodec.encode(entry.getKey())) {
                        case CodecResult.Success<JsonElement> key when
                                !key.value().isJsonPrimitive() || !key.value().getAsJsonPrimitive().isString() -> {
                            return CodecResult.failure("Failed to encode key '" + key + "' expected a string!");
                        }
                        case CodecResult.Success<JsonElement> key -> {
                            switch (valueCodec.encode(entry.getValue())) {
                                case CodecResult.Success<JsonElement> value -> object.add(key.value().getAsString(), value.value());
                                case CodecResult.Failure<?> failure -> {
                                    return CodecResult.failure("Failed to encode value for field '" + key.value() + "' due to: " + failure.error());
                                }
                            }
                        }
                        case CodecResult.Failure<JsonElement> failure -> {
                            return failure;
                        }
                    }
                }

                return CodecResult.success(object);
            }
        };

    }

    static <T, C extends Collection<T>> RepoCodec<C> collection(
            RepoCodec<T> elementCodec,
            Function<List<T>, C> factory
    ) {
        return new RepoCodec<>() {
            @Override
            public CodecResult<C> decode(JsonElement element) {
                if (element instanceof JsonArray array) {
                    var list = new ArrayList<T>();
                    for (JsonElement entry : array) {
                        switch (elementCodec.decode(entry)) {
                            case CodecResult.Success<T> success -> list.add(success.value());
                            case CodecResult.Failure<T> failure -> {
                                return CodecResult.failure(failure.error());
                            }
                        }
                    }
                    return CodecResult.success(factory.apply(list));
                }
                return CodecResult.failure("Expected an array");
            }

            @Override
            public CodecResult<JsonElement> encode(C value) {
                var array = new JsonArray();
                for (T element : value) {
                    switch (elementCodec.encode(element)) {
                        case CodecResult.Success<JsonElement> success -> array.add(success.value());
                        case CodecResult.Failure<JsonElement> failure -> {
                            return failure;
                        }
                    }
                }
                return CodecResult.success(array);
            }
        };
    }

    static <A> RepoCodec<A> orElse(RepoCodec<A> primary, List<RepoCodec<A>> codecs) {
        if (codecs.isEmpty()) {
            throw new IllegalArgumentException("At least one codec must be provided");
        }

        return new RepoCodec<>() {
            @Override
            public CodecResult<A> decode(JsonElement element) {
                List<String> errors = new ArrayList<>();
                switch (primary.decode(element)) {
                    case CodecResult.Success<A> success -> {
                        return success;
                    }
                    case CodecResult.Failure<A> failure -> errors.add(failure.error());
                }

                for (RepoCodec<A> codec : codecs) {
                    switch (codec.decode(element)) {
                        case CodecResult.Success<A> success -> {
                            return success;
                        }
                        case CodecResult.Failure<A> failure -> errors.add(failure.error());
                    }
                }
                return CodecResult.failure("All codecs failed: " + String.join("; ", errors));
            }

            @Override
            public CodecResult<JsonElement> encode(A value) {
                return primary.encode(value);
            }
        };
    }
}
