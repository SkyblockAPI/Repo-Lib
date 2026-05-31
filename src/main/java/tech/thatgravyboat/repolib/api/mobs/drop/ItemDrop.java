package tech.thatgravyboat.repolib.api.mobs.drop;

import com.google.gson.JsonObject;
import org.jetbrains.annotations.NotNull;
import tech.thatgravyboat.repolib.internal.JsonHelper;

public record ItemDrop(
        @NotNull String id,
        int minAmount,
        int maxAmount,
        float chance
) implements MobDrop {

    static @NotNull ItemDrop fromJson(@NotNull JsonObject json) {
        return new ItemDrop(
                json.get("id").getAsString(),
                JsonHelper.getInt(json, "minAmount", 1),
                JsonHelper.getInt(json, "maxAmount", 1),
                JsonHelper.getFloat(json, "chance", 1)
        );
    }

    @Override
    public String type() {
        return "item";
    }
}
