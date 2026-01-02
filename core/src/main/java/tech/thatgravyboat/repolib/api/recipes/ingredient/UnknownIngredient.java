package tech.thatgravyboat.repolib.api.recipes.ingredient;

public record UnknownIngredient(
        Object value,
        int count
) implements CraftingIngredient{
    @Override
    public String type() {
        return "unknown";
    }
}
