package tech.thatgravyboat.repolib.v2.api.properties;

import tech.thatgravyboat.repolib.v2.api.id.GlobalSkyBlockId;
import tech.thatgravyboat.repolib.v2.internal.codec.CodecResult;
import tech.thatgravyboat.repolib.v2.internal.codec.RepoCodec;
import tech.thatgravyboat.repolib.v2.internal.codec.struct.StructRepoCodec;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public record IdProperty(
        String name,
        Set<String> values
) {

    public static RepoCodec<IdProperty> CODEC = StructRepoCodec.of(
            IdProperty::new,
            RepoCodec.STRING.fieldOf("name"),
            IdProperty::name,
            RepoCodec.STRING.setOf().fieldOf("values"),
            IdProperty::values
    );

    public static RepoCodec<Map<IdProperty, String>> stringCodec(Set<IdProperty> allowedProperties) {
        return RepoCodec.STRING.flatMap(
                properties -> {
                    var map = new HashMap<IdProperty, String>();

                    properties:
                    for (var entry : properties.split(",")) {
                        var entries = entry.split("=");
                        var name = entries[0];
                        var value = entries[1];

                        for (var validProperty : allowedProperties) {
                            if (!validProperty.name().equals(name)) {
                                continue;
                            }
                            if (validProperty.values().contains(value)) {
                                map.put(validProperty, value);
                                continue properties;
                            }

                            return CodecResult.failure("Unknown property value '%s' for property '%s', allowed values are [%s]".formatted(
                                    value,
                                    name,
                                    String.join(",", validProperty.values())
                            ));
                        }

                        return CodecResult.failure("Unknown property '%s'".formatted(name));
                    }

                    return CodecResult.success(map);
                },
                properties -> {
                    var builder = new StringBuilder();
                    for (var entry : properties.entrySet()) {
                        builder.append(entry.getKey()).append("=").append(entry.getValue()).append(",");
                    }
                    builder.deleteCharAt(builder.length() - 1);
                    return CodecResult.success(builder.toString());
                }
        );
    }
}
