package tech.thatgravyboat.repolib.api.recipes.ingredient;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.jetbrains.annotations.ApiStatus;

public interface CraftingIngredient {

    String type();

    int count();

    @ApiStatus.Internal
    static CraftingIngredient parse(JsonObject json) {
        JsonElement type = json.get("type");
        return switch (type == null ? "item" : type.getAsString()) {
            case "pet" -> PetIngredient.fromJson(json);
            case "item" -> ItemIngredient.fromJson(json);
            case "enchantment" -> EnchantmentIngredient.fromJson(json);
            case "attribute" -> AttributeIngredient.fromJson(json);
            default -> new UnknownIngredient(json, 1);
        };
    }
}
