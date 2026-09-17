package tech.thatgravyboat.repolib.api.mobs;

import com.google.gson.JsonObject;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import tech.thatgravyboat.repolib.api.types.Position;

import java.util.List;

public record Mob(
        @Nullable String island,
        @Nullable Position position,
        @Deprecated @Nullable String texture,
        @Deprecated @NotNull String itemId,
        @NotNull String name,
        @Nullable String type,
        List<LootTable> lootTables,
        @NotNull JsonObject item
) {
}
