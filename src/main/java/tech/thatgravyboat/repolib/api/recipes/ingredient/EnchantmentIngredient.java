package tech.thatgravyboat.repolib.api.recipes.ingredient;

import com.google.gson.JsonObject;
import org.jetbrains.annotations.NotNull;

public record EnchantmentIngredient(
        String id,
        int level,
        int count
) implements CraftingIngredient {

    public static @NotNull EnchantmentIngredient fromJson(@NotNull JsonObject json) {
        return new EnchantmentIngredient(
                json.get("id").getAsString(),
                json.get("level").getAsInt(),
                json.get("count").getAsInt()
        );
    }

    @Override
    public String type() {
        return "enchantment";
    }
}
