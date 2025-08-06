package tech.thatgravyboat.repolib.api;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import tech.thatgravyboat.repolib.api.recipes.Recipe;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class RecipesAPI {

    private final Map<Recipe.Type<?>, List<Recipe<?>>> recipes = new HashMap<>();

    void load(JsonElement json) {
        Map<Recipe.Type<?>, List<Recipe<?>>> recipes = new HashMap<>();
        if (json instanceof JsonArray array) {
            for (var element : array) {
                Recipe<?> recipe = Recipe.parse(element.getAsJsonObject());
                if (recipe == null) continue;
                recipes.computeIfAbsent(recipe.type(), k -> new ArrayList<>()).add(recipe);
            }
        }
        recipes.forEach((type, list) -> this.recipes.put(type, List.copyOf(list)));
    }

    @SuppressWarnings("unchecked")
    public <T extends Recipe<T>> List<T> getRecipes(Recipe.Type<T> type) {
        return (List<T>) recipes.getOrDefault(type, List.of());
    }
}
