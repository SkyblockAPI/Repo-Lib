package tech.thatgravyboat.repolib.api.recipes;

import com.google.gson.JsonObject;
import org.jetbrains.annotations.NotNull;
import tech.thatgravyboat.repolib.api.recipes.ingredient.CraftingIngredient;
import tech.thatgravyboat.repolib.api.recipes.ingredient.EmptyIngredient;
import tech.thatgravyboat.repolib.internal.JsonHelper;

import java.util.List;

public record CraftingRecipe(
        @NotNull List<CraftingIngredient> inputs,
        @NotNull CraftingIngredient result
) implements Recipe<CraftingRecipe> {

    static @NotNull CraftingRecipe fromJson(@NotNull JsonObject json) {
        List<CraftingIngredient> keys = JsonHelper.getList(json, "keys", it -> CraftingIngredient.parse(it.getAsJsonObject()));
        return new CraftingRecipe(
                JsonHelper.getList(json, "pattern", it ->  it.getAsInt() == -1 ? EmptyIngredient.INSTANCE : keys.get(it.getAsInt())),
                CraftingIngredient.parse(json.getAsJsonObject("result"))
        );
    }

    @Override
    public Type<CraftingRecipe> type() {
        return Type.CRAFTING;
    }
}
