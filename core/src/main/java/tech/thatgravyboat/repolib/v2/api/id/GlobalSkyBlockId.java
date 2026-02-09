package tech.thatgravyboat.repolib.v2.api.id;

import com.google.gson.JsonPrimitive;
import tech.thatgravyboat.repolib.v2.api.properties.IdProperty;
import tech.thatgravyboat.repolib.v2.api.types.SkyBlockIdType;
import tech.thatgravyboat.repolib.v2.internal.codec.CodecResult;
import tech.thatgravyboat.repolib.v2.internal.codec.RepoCodec;
import tech.thatgravyboat.repolib.v2.internal.utils.JsonUtils;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.stream.Collectors;

public record GlobalSkyBlockId(
        BaseSkyBlockId base,
        Map<IdProperty, String> properties
) implements GlobalSkyblockIdRepresentable {

    public static RepoCodec<GlobalSkyBlockId> CODEC = RepoCodec.STRING.flatMap(
            string -> {
                var split = string.indexOf('[');
                if (split == -1) {
                    return BaseSkyBlockId.CODEC.decode(new JsonPrimitive(string)).map(GlobalSkyBlockId::new);
                }

                var base = BaseSkyBlockId.CODEC.decode(new JsonPrimitive(string.substring(0, split)));
                if (base.isFailure()) {
                    return base.into();
                }
                var end = string.lastIndexOf(']');
                if (end != string.length() - 1) {
                    return CodecResult.failure("Malformed skyblock globalId '%s', expected ']' at index %d".formatted(string, string.length() - 1));
                }

                var baseValue = base.orElseThrow();

                var codec = IdProperty.stringCodec(new HashSet<>(baseValue.allowedProperties()));
                var map = codec.decode(new JsonPrimitive(string.substring(split + 1)));

                return CodecResult.success(new GlobalSkyBlockId(baseValue, map.orElseThrow()));
            },
            id -> {
                var base = BaseSkyBlockId.CODEC.encode(id.baseId());
                if (base.isFailure()) {
                    return base.into();
                }

                var baseValue = base.orElseThrow();
                if (!JsonUtils.isString(baseValue)) {
                    return CodecResult.failure(JsonUtils.format("base globalId", "string", baseValue));
                }

                var builder = new StringBuilder();
                builder.append(baseValue.getAsString());
                if (id.properties.isEmpty()) {
                    return CodecResult.success(builder.toString());
                }

                builder.append("[");

                var codec = IdProperty.stringCodec(new HashSet<>(id.allowedProperties()));
                var propertiesValue = codec.encode(id.properties).orElseThrow();
                if (!JsonUtils.isString(propertiesValue)) {
                    return CodecResult.failure(JsonUtils.format("properties", "string", baseValue));
                }

                builder.append(propertiesValue.getAsString());
                builder.append("]");

                return CodecResult.success(builder.toString());
            }
    );

    public GlobalSkyBlockId(BaseSkyBlockId base) {
        this(base, new HashMap<>());
    }

    public GlobalSkyBlockId(SkyBlockIdType type, String id, Map<IdProperty, String> properties) {
        this(new BaseSkyBlockId(type, id), properties);
    }

    @Override
    public String toString() {
        var builder = new StringBuilder();

        builder.append(this.base);

        if (!properties.isEmpty()) {
            builder.append("[");
            builder.append(properties.entrySet()
                    .stream()
                    .map((entry) -> entry.getKey().name() + "=" + entry.getValue())
                    .collect(Collectors.joining(",")));
            builder.append("]");
        }

        return builder.toString();
    }

    @Override
    public GlobalSkyBlockId globalId() {
        return this;
    }
}
