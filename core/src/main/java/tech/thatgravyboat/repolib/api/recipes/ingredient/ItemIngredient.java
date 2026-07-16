package tech.thatgravyboat.repolib.api.recipes.ingredient;

import com.google.gson.JsonObject;
import org.jetbrains.annotations.NotNull;
import tech.thatgravyboat.repolib.internal.JsonHelper;

public record ItemIngredient(
        @NotNull String id,
        int count
) implements CraftingIngredient {

    static @NotNull ItemIngredient fromJson(@NotNull JsonObject json) {
        return new ItemIngredient(
                json.get("id").getAsString(),
                JsonHelper.getInt(json, "count", 1)
        );
    }

    @Override
    public String type() {
        return "item";
    }
}
