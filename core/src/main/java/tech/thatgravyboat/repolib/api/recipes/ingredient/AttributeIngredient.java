package tech.thatgravyboat.repolib.api.recipes.ingredient;

import com.google.gson.JsonObject;
import org.jetbrains.annotations.NotNull;
import tech.thatgravyboat.repolib.internal.JsonHelper;

public record AttributeIngredient(
        String id,
        int count
) implements CraftingIngredient {


    static @NotNull AttributeIngredient fromJson(@NotNull JsonObject json) {
        return new AttributeIngredient(
                json.get("id").getAsString(),
                JsonHelper.getInt(json, "count", 1)
        );
    }


    @Override
    public String type() {
        return "attribute";
    }
}
