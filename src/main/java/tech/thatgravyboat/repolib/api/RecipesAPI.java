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

    static RecipesAPI load(JsonElement json) {
        RecipesAPI api = new RecipesAPI();
        Map<Recipe.Type<?>, List<Recipe<?>>> recipes = new HashMap<>();
        if (json instanceof JsonArray array) {
            for (var element : array) {
                Recipe<?> recipe = Recipe.parse(element.getAsJsonObject());
                recipes.computeIfAbsent(recipe.type(), k -> new ArrayList<>()).add(recipe);
            }
        }
        recipes.forEach((type, list) -> api.recipes.put(type, List.copyOf(list)));
        return api;
    }

    @SuppressWarnings("unchecked")
    public <T extends Recipe<T>> List<T> getRecipes(Recipe.Type<T> type) {
        return (List<T>) recipes.getOrDefault(type, List.of());
    }
}
