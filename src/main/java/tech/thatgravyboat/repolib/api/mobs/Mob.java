package tech.thatgravyboat.repolib.api.mobs;

import org.jetbrains.annotations.Nullable;
import tech.thatgravyboat.repolib.api.types.Position;

public record Mob(
        @Nullable String island,
        @Nullable Position position,
        @Nullable String texture
) {
}
