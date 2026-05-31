package tech.thatgravyboat.repolib.api.mobs.drop;

import com.google.gson.JsonObject;
import org.jetbrains.annotations.NotNull;
import tech.thatgravyboat.repolib.internal.JsonHelper;

public record AttributeDrop(
        String id,
        int minAmount,
        int maxAmount,
        float chance
) implements MobDrop {


    static @NotNull AttributeDrop fromJson(@NotNull JsonObject json) {
        return new AttributeDrop(
                json.get("id").getAsString(),
                JsonHelper.getInt(json, "minAmount", 1),
                JsonHelper.getInt(json, "maxAmount", 1),
                JsonHelper.getFloat(json, "chance", 1)
        );
    }


    @Override
    public String type() {
        return "attribute";
    }
}
