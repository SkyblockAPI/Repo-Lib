package tech.thatgravyboat.repolib.api.mobs.drop;

import com.google.gson.JsonObject;
import org.jetbrains.annotations.NotNull;
import tech.thatgravyboat.repolib.internal.JsonHelper;

public record PetDrop(
        @NotNull String id,
        @NotNull String tier,
        int minAmount,
        int maxAmount,
        float chance
) implements MobDrop {

    static @NotNull PetDrop fromJson(@NotNull JsonObject json) {
        return new PetDrop(
                json.get("pet").getAsString(),
                json.get("tier").getAsString(),
                JsonHelper.getInt(json, "minAmount", 1),
                JsonHelper.getInt(json, "maxAmount", 1),
                JsonHelper.getFloat(json, "chance", 1)
        );
    }

    @Override
    public String type() {
        return "pet";
    }
}
