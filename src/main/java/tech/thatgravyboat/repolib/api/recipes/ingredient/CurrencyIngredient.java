package tech.thatgravyboat.repolib.api.recipes.ingredient;

import com.google.gson.JsonObject;
import org.jetbrains.annotations.NotNull;
import tech.thatgravyboat.repolib.internal.JsonHelper;

public record CurrencyIngredient(
        String currency,
        int count
) implements CraftingIngredient {


    static @NotNull CurrencyIngredient fromJson(@NotNull JsonObject json) {
        return new CurrencyIngredient(
                json.get("currency").getAsString(),
                JsonHelper.getInt(json, "count", 1)
        );
    }


    @Override
    public String type() {
        return "currency";
    }
}

