package tech.thatgravyboat.repolib.api.recipes;

import com.google.gson.JsonObject;
import org.jetbrains.annotations.NotNull;
import tech.thatgravyboat.repolib.internal.JsonHelper;

public interface Recipe<T extends Recipe<T>> {

    Type<T> type();

    record Type<T extends Recipe<T>>(String type, Class<T> clazz) {
        public static final Type<CraftingRecipe> CRAFTING = new Type<>("crafting", CraftingRecipe.class);
        public static final Type<ForgeRecipe> FORGE = new Type<>("forge", ForgeRecipe.class);
    }

    static Recipe<?> parse(@NotNull JsonObject json) {
        return switch (JsonHelper.getString(json, "type", "crafting")) {
            case "crafting" -> CraftingRecipe.fromJson(json);
            case "forge" -> ForgeRecipe.fromJson(json);
            default -> throw new IllegalArgumentException("Unknown recipe type");
        };
    }
}
