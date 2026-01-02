package tech.thatgravyboat.repolib.v2.api;

import org.jetbrains.annotations.NotNull;
import tech.thatgravyboat.repolib.internal.Utils;
import tech.thatgravyboat.repolib.v2.api.properties.IdProperty;

import java.util.Map;
import java.util.stream.Collectors;

public record GlobalSkyBlockId(
        SkyBlockIdType type,
        String id,
        Map<IdProperty<?>, ?> properties
) {

    @Override
    public @NotNull String toString() {
        var builder = new StringBuilder();

        builder.append(type.getName());
        builder.append(":");
        builder.append(id);


        if (!properties.isEmpty()) {
            builder.append("[");
            builder.append(properties.entrySet()
                    .stream()
                    .map((entry) ->
                            entry.getKey().getName() + "=" + entry.getKey().serialize(
                                    Utils.unsafe(entry.getValue())
                            ))
                    .collect(Collectors.joining(",")));
            builder.append("]");
        }


        return builder.toString();
    }
}
