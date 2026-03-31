package tech.thatgravyboat.repolib.api.recipes.ingredient;

import com.google.gson.JsonObject;
import org.jetbrains.annotations.NotNull;

public record PotionIngredient(
        String id,
        int level,
        int count
) implements CraftingIngredient {

    public static @NotNull PotionIngredient fromJson(@NotNull JsonObject json) {
        return new PotionIngredient(
                json.get("id").getAsString(),
                json.get("level").getAsInt(),
                json.get("count").getAsInt()
        );
    }

    @Override
    public String type() {
        return "potion";
    }
}
