package tech.thatgravyboat.repolib.api.recipes.ingredient;

import com.google.gson.JsonObject;
import org.jetbrains.annotations.NotNull;

public record CurrencyIngredient(
        String currency,
        int count
) implements CraftingIngredient {


    static @NotNull CurrencyIngredient fromJson(@NotNull JsonObject json) {
        return new CurrencyIngredient(
                json.get("currency").getAsString(),
                json.get("count").getAsInt()
        );
    }


    @Override
    public String type() {
        return "currency";
    }
}

