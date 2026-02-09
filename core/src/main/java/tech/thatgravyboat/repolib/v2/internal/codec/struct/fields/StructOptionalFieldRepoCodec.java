package tech.thatgravyboat.repolib.v2.internal.codec.struct.fields;

import com.google.gson.JsonObject;
import tech.thatgravyboat.repolib.v2.internal.codec.CodecResult;
import tech.thatgravyboat.repolib.v2.internal.codec.RepoCodec;

import java.util.Optional;

public record StructOptionalFieldRepoCodec<Type>(
        String field,
        RepoCodec<Type> codec
) implements StructFieldRepoCodec<Optional<Type>> {

    @Override
    public CodecResult<Optional<Type>> decode(JsonObject object) {
        if (object.has(field)) {
            return codec.decode(object.get(field)).map(Optional::of);
        }
        return CodecResult.success(Optional.empty());
    }

    @Override
    public CodecResult<Void> encode(JsonObject object, Optional<Type> value) {
        if (value.isPresent()) {
            return codec.encode(value.get()).map(json -> {
                object.add(field, json);
                return null;
            });
        }
        return CodecResult.success(null);
    }
}
