package tech.thatgravyboat.repolib.api.mobs.drop;

import com.google.gson.JsonObject;
import org.jetbrains.annotations.NotNull;
import tech.thatgravyboat.repolib.internal.JsonHelper;

public record UnknownDrop(
        Object value,
        int minAmount,
        int maxAmount,
        float chance
) implements MobDrop {

    public static @NotNull UnknownDrop fromJson(@NotNull JsonObject json) {
        return new UnknownDrop(
                json,
                JsonHelper.getInt(json, "minAmount", 1),
                JsonHelper.getInt(json, "maxAmount", 1),
                JsonHelper.getFloat(json, "chance", 1)
        );
    }

    @Override
    public String type() {
        return "unknown";
    }
}
