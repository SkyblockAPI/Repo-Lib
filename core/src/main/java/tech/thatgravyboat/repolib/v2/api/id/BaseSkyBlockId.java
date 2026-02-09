package tech.thatgravyboat.repolib.v2.api.id;


import com.google.gson.JsonPrimitive;
import tech.thatgravyboat.repolib.v2.api.types.SkyBlockIdType;
import tech.thatgravyboat.repolib.v2.internal.codec.CodecResult;
import tech.thatgravyboat.repolib.v2.internal.codec.RepoCodec;
import tech.thatgravyboat.repolib.v2.internal.utils.JsonUtils;

public record BaseSkyBlockId(
        SkyBlockIdType type,
        String id
) implements BaseSkyblockIdRepresentable {
    public static RepoCodec<BaseSkyBlockId> CODEC = RepoCodec.STRING.flatMap(
            string -> {
                var split = string.indexOf(':');
                if (split == -1) {
                    return CodecResult.failure("Invalid global skyblock globalId, '" + string + "' doesnt contain a ':'");
                }

                var type = SkyBlockIdType.CODEC.decode(new JsonPrimitive(string.substring(0, split)));
                if (type.isFailure()) return type.into();

                return CodecResult.success(new BaseSkyBlockId(type.orElseThrow(), string.substring(split + 1)));
            },
            id -> {
                var type = SkyBlockIdType.CODEC.encode(id.type());
                if (type.isFailure()) return type.into();

                var typeValue = type.orElseThrow();
                if (!JsonUtils.isString(typeValue)) return CodecResult.failure(JsonUtils.format("type", "string", typeValue));

                return CodecResult.success(typeValue.getAsString() + ":" + id.id());
            }
    );

    @Override
    public String toString() {
        return type.name() + ":" + id;
    }

    @Override
    public BaseSkyBlockId baseId() {
        return this;
    }
}