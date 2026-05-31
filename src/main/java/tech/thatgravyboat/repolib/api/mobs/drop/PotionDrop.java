package tech.thatgravyboat.repolib.api.mobs.drop;

import com.google.gson.JsonObject;
import org.jetbrains.annotations.NotNull;
import tech.thatgravyboat.repolib.internal.JsonHelper;

public record PotionDrop(
        String id,
        int level,
        int minAmount,
        int maxAmount,
        float chance
) implements MobDrop {

    public static @NotNull PotionDrop fromJson(@NotNull JsonObject json) {
        return new PotionDrop(
                json.get("id").getAsString(),
                json.get("level").getAsInt(),
                JsonHelper.getInt(json, "minAmount", 1),
                JsonHelper.getInt(json, "maxAmount", 1),
                JsonHelper.getFloat(json, "chance", 1)
        );
    }

    @Override
    public String type() {
        return "potion";
    }
}
