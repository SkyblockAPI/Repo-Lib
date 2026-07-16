package tech.thatgravyboat.repolib.api.recipes.ingredient;

import com.google.gson.JsonObject;
import org.jetbrains.annotations.NotNull;
import tech.thatgravyboat.repolib.internal.JsonHelper;

public record RuneIngredient(
        String id,
        int tier,
        int count
) implements CraftingIngredient {

    public static @NotNull RuneIngredient fromJson(@NotNull JsonObject json) {
        return new RuneIngredient(
                json.get("id").getAsString(),
                json.get("tier").getAsInt(),
                JsonHelper.getInt(json, "count", 1)
        );
    }

    @Override
    public String type() {
        return "rune";
    }
}
