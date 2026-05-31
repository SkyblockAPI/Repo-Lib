package tech.thatgravyboat.repolib.api.mobs;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import tech.thatgravyboat.repolib.api.types.Position;

import java.util.List;

public record Mob(
        @Nullable String island,
        @Nullable Position position,
        @Nullable String texture,
        @NotNull String itemId,
        @NotNull String name,
        @Nullable String type,
        List<LootTable> lootTables
) {
}
