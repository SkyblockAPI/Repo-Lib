package tech.thatgravyboat.repolib.v2.internal.codec.struct.fields;

import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import tech.thatgravyboat.repolib.v2.internal.codec.CodecResult;
import tech.thatgravyboat.repolib.v2.internal.codec.RepoCodec;
import tech.thatgravyboat.repolib.v2.internal.utils.JsonUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public record StructKeyDispatchCodec<Key, Value>(
        RepoCodec<Key> keyCodec,
        Function<Key, RepoCodec<Value>> valueCodec
) implements StructFieldRepoCodec<Map<Key, Value>> {
    @Override
    public CodecResult<Map<Key, Value>> decode(JsonObject object) throws CodecResult.ResultException {
        var map = new HashMap<Key, Value>();
        for (var entry : object.entrySet()) {

            var key = keyCodec.decode(new JsonPrimitive(entry.getKey()));
            if (key.isFailure()) {
                return key.into();
            }
            var keyValue = key.orElseThrow();

            var value = valueCodec.apply(keyValue).decode(entry.getValue());
            if (value.isFailure()) {
                return value.into();
            }
            map.put(keyValue, value.orElseThrow());
        }

        return CodecResult.success(map);
    }

    @Override
    public CodecResult<Void> encode(JsonObject object, Map<Key, Value> map) throws CodecResult.ResultException {
        for (var entry : map.entrySet()) {
            var key = keyCodec.encode(entry.getKey());
            if (key.isFailure()) {
                return key.into();
            }

            var value = valueCodec.apply(entry.getKey()).encode(entry.getValue());
            if (value.isFailure()) {
                return value.into();
            }

            var keyValue = key.orElseThrow();
            if (JsonUtils.isString(keyValue)) {
                return CodecResult.failure("Expected key to be a string but got '" + keyValue + "' (" + JsonUtils.type(keyValue) + ")!");
            }

            object.add(keyValue.getAsString(), value.orElseThrow());
        }

        return CodecResult.success(null);
    }
}
