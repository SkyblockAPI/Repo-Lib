package tech.thatgravyboat.repolib.api.recipes;

import com.google.gson.JsonObject;
import org.jetbrains.annotations.NotNull;
import tech.thatgravyboat.repolib.api.recipes.ingredient.CraftingIngredient;
import tech.thatgravyboat.repolib.internal.JsonHelper;

import java.util.List;

public record KatRecipe(
        @NotNull CraftingIngredient input,
        @NotNull List<CraftingIngredient> items,
        int coins,
        int time,
        @NotNull CraftingIngredient output
) implements Recipe<KatRecipe> {

    static @NotNull KatRecipe fromJson(@NotNull JsonObject json) {
        return new KatRecipe(
                CraftingIngredient.parse(json.getAsJsonObject("input")),
                JsonHelper.getList(json, "items", it -> CraftingIngredient.parse(it.getAsJsonObject())),
                JsonHelper.getInt(json, "coins", 0),
                JsonHelper.getInt(json, "time", 0),
                CraftingIngredient.parse(json.getAsJsonObject("output"))
        );
    }

    @Override
    public Type<KatRecipe> type() {
        return Type.KAT;
    }
}

