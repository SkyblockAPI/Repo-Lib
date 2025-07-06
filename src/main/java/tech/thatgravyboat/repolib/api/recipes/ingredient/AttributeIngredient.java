package tech.thatgravyboat.repolib.api.recipes.ingredient;

import com.google.gson.JsonObject;
import org.jetbrains.annotations.NotNull;

public record AttributeIngredient(
        String id,
        int count
) implements CraftingIngredient {


    static @NotNull AttributeIngredient fromJson(@NotNull JsonObject json) {
        return new AttributeIngredient(
                json.get("id").getAsString(),
                json.get("count").getAsInt()
        );
    }


    @Override
    public String type() {
        return "attribute";
    }
}
