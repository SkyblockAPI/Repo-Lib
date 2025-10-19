package tech.thatgravyboat.repolib.api.recipes;

import com.google.gson.JsonObject;
import org.jetbrains.annotations.NotNull;
import tech.thatgravyboat.repolib.api.recipes.ingredient.CraftingIngredient;
import tech.thatgravyboat.repolib.internal.JsonHelper;

import java.util.List;

public record ShopRecipe(
        @NotNull List<CraftingIngredient> inputs,
        @NotNull CraftingIngredient result
) implements Recipe<ShopRecipe> {

    static @NotNull ShopRecipe fromJson(@NotNull JsonObject json) {
        return new ShopRecipe(
                JsonHelper.getList(json, "inputs", it -> CraftingIngredient.parse(it.getAsJsonObject())),
                CraftingIngredient.parse(json.getAsJsonObject("result"))
        );
    }

    @Override
    public Type<ShopRecipe> type() {
        return Type.SHOP;
    }
}

