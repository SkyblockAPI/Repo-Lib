package tech.thatgravyboat.repolib.v2.internal.codec.struct.fields;

import com.google.gson.JsonObject;
import tech.thatgravyboat.repolib.v2.internal.codec.CodecResult;
import tech.thatgravyboat.repolib.v2.internal.codec.struct.Functions.*;

public record StructDispatchFieldRepoCodec<Key, Type>(
        StructFieldRepoCodec<Key> keyCodec,
        Function1<Key, CodecResult<StructFieldRepoCodec<Type>>> dispatch,
        Function1<Type, CodecResult<Key>> keyExtractor
) implements StructFieldRepoCodec<Type> {

    @Override
    public CodecResult<Type> decode(JsonObject object) {
        var key = keyCodec.decode(object).orElseThrow();
        var fieldCodec = dispatch.apply(key).orElseThrow();
        return fieldCodec.decode(object);
    }

    @Override
    public CodecResult<Void> encode(JsonObject object, Type value) {
        var key = keyExtractor.apply(value).orElseThrow();
        dispatch.apply(key).orElseThrow().encode(object, value).orElseThrow();

        // encode key AFTER value to ensure that the key field is equal to the key
        return keyCodec.encode(object, key);
    }
}
