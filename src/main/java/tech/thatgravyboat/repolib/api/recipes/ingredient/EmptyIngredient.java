package tech.thatgravyboat.repolib.api.recipes.ingredient;

public class EmptyIngredient implements CraftingIngredient {

    public static final EmptyIngredient INSTANCE = new EmptyIngredient();

    private EmptyIngredient() {
    }

    @Override
    public String type() {
        return "empty";
    }

    @Override
    public int count() {
        return 0;
    }
}
