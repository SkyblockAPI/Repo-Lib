package tech.thatgravyboat.repolib.v2.internal.codec.struct.fields;

import com.google.gson.JsonObject;
import tech.thatgravyboat.repolib.v2.internal.codec.CodecResult;
import tech.thatgravyboat.repolib.v2.internal.codec.RepoCodec;

public record StructNormalFieldRepoCodec<Type>(
        String field, RepoCodec<Type> codec
) implements StructFieldRepoCodec<Type> {

    @Override
    public CodecResult<Type> decode(JsonObject object) {
        if (object.has(field)) {
            return codec.decode(object.get(field));
        }
        return CodecResult.failure("Missing field: " + field);
    }

    @Override
    public CodecResult<Void> encode(JsonObject object, Type value) {
        var encoded = codec.encode(value).orElseThrow();
        object.add(field, encoded);
        return CodecResult.success(null);
    }
}
