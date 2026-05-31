package tech.thatgravyboat.repolib.api.mobs.drop;

import com.google.gson.JsonObject;
import org.jetbrains.annotations.NotNull;
import tech.thatgravyboat.repolib.internal.JsonHelper;

public record RuneDrop(
        String id,
        int tier,
        int minAmount,
        int maxAmount,
        float chance
) implements MobDrop {

    public static @NotNull RuneDrop fromJson(@NotNull JsonObject json) {
        return new RuneDrop(
                json.get("id").getAsString(),
                json.get("tier").getAsInt(),
                JsonHelper.getInt(json, "minAmount", 1),
                JsonHelper.getInt(json, "maxAmount", 1),
                JsonHelper.getFloat(json, "chance", 1)
        );
    }

    @Override
    public String type() {
        return "rune";
    }
}
