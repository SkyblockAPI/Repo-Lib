package tech.thatgravyboat.repolib.api.recipes.ingredient;

import com.google.gson.JsonObject;
import org.jetbrains.annotations.NotNull;
import tech.thatgravyboat.repolib.internal.JsonHelper;

public record PotionIngredient(
        String id,
        int level,
        int count
) implements CraftingIngredient {

    public static @NotNull PotionIngredient fromJson(@NotNull JsonObject json) {
        return new PotionIngredient(
                json.get("id").getAsString(),
                json.get("level").getAsInt(),
                JsonHelper.getInt(json, "count", 1)
        );
    }

    @Override
    public String type() {
        return "potion";
    }
}
