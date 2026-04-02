package tech.thatgravyboat.repolib.api.recipes.ingredient;

import com.google.gson.JsonObject;
import org.jetbrains.annotations.NotNull;
import tech.thatgravyboat.repolib.internal.JsonHelper;

public record PetIngredient(
        @NotNull String id,
        @NotNull String tier,
        int count
) implements CraftingIngredient {

    static @NotNull PetIngredient fromJson(@NotNull JsonObject json) {
        return new PetIngredient(
                json.get("pet").getAsString(),
                json.get("tier").getAsString(),
                JsonHelper.getInt(json, "count", 1)
        );
    }

    @Override
    public String type() {
        return "pet";
    }
}
