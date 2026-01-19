package tech.thatgravyboat.repolib.v2.api;

import org.jetbrains.annotations.NotNull;
import tech.thatgravyboat.repolib.internal.Utils;
import tech.thatgravyboat.repolib.v2.api.properties.IdProperty;
import tech.thatgravyboat.repolib.v2.api.types.SkyBlockIdType;

import java.util.Map;
import java.util.stream.Collectors;

public record GlobalSkyBlockId(
        GlobalSkyBlockBase base,
        Map<IdProperty<?>, ?> properties
) {

    public GlobalSkyBlockId(SkyBlockIdType type, String id, Map<IdProperty<?>, ?> properties) {
        this(new GlobalSkyBlockBase(type, id), properties);
    }

    @Override
    public @NotNull String toString() {
        var builder = new StringBuilder();

        builder.append(this.base.toString());

        if (!properties.isEmpty()) {
            builder.append("[");
            builder.append(properties.entrySet()
                    .stream()
                    .map((entry) ->
                            entry.getKey().name() + "=" + entry.getKey().serialize(
                                    Utils.unsafe(entry.getValue())
                            ))
                    .collect(Collectors.joining(",")));
            builder.append("]");
        }

        return builder.toString();
    }

    public SkyBlockIdType type() {
        return this.base.type();
    }

    public String id() {
        return this.base.id();
    }

    public Iterable<GlobalSkyBlockId> variants() {
        return base.type().getVariants(this);
    }
}
