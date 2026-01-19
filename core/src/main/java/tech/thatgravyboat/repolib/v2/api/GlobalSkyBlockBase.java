package tech.thatgravyboat.repolib.v2.api;


import org.jetbrains.annotations.NotNull;
import tech.thatgravyboat.repolib.v2.api.types.SkyBlockIdType;

public record GlobalSkyBlockBase(
        SkyBlockIdType type,
        String id
) {
    @Override
    public @NotNull String toString() {
        return type.name() + ":" + id;
    }

}