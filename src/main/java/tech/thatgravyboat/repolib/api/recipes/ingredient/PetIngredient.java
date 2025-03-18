package tech.thatgravyboat.repolib.api.recipes.ingredient;

import com.google.gson.JsonObject;
import org.jetbrains.annotations.NotNull;

public record PetIngredient(
        @NotNull String id,
        @NotNull String tier,
        int count
) implements CraftingIngredient {

    static @NotNull PetIngredient fromJson(@NotNull JsonObject json) {
        return new PetIngredient(
                json.get("pet").getAsString(),
                json.get("tier").getAsString(),
                json.get("count").getAsInt()
        );
    }

    @Override
    public String type() {
        return "pet";
    }
}
