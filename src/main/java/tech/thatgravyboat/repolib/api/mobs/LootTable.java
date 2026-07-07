package tech.thatgravyboat.repolib.api.mobs;

import org.jetbrains.annotations.NotNull;
import tech.thatgravyboat.repolib.api.mobs.drop.MobDrop;

import java.util.List;

public record LootTable(
        @NotNull String name,
        int mobLevel,
        int xp,
        int combatXp,
        @NotNull List<MobDrop> drops
) {
    /**
     * Use coins from the drops list instead
     * @return
     */
    @Deprecated
    int coins() {
        return 0;
    }
}
