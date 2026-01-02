package tech.thatgravyboat.repolib.api.recipes;

import com.google.gson.JsonObject;
import org.jetbrains.annotations.NotNull;
import tech.thatgravyboat.repolib.api.recipes.ingredient.CraftingIngredient;
import tech.thatgravyboat.repolib.internal.JsonHelper;

import java.util.List;

public record ForgeRecipe(
        @NotNull List<CraftingIngredient> inputs,
        int coins,
        int time,
        @NotNull CraftingIngredient result
) implements Recipe<ForgeRecipe> {

    static @NotNull ForgeRecipe fromJson(@NotNull JsonObject json) {
        return new ForgeRecipe(
                JsonHelper.getList(json, "inputs", it -> CraftingIngredient.parse(it.getAsJsonObject())),
                JsonHelper.getInt(json, "coins", 0),
                JsonHelper.getInt(json, "time", 0),
                CraftingIngredient.parse(json.getAsJsonObject("result"))
        );
    }

    @Override
    public Type<ForgeRecipe> type() {
        return Type.FORGE;
    }
}
