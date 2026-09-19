package tech.thatgravyboat.repolib.api;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.jetbrains.annotations.Nullable;
import tech.thatgravyboat.repolib.api.idoverlays.Requirement;
import tech.thatgravyboat.repolib.api.recipes.ingredient.AttributeIngredient;
import tech.thatgravyboat.repolib.api.recipes.ingredient.CraftingIngredient;
import tech.thatgravyboat.repolib.api.recipes.ingredient.EnchantmentIngredient;
import tech.thatgravyboat.repolib.api.recipes.ingredient.ItemIngredient;
import tech.thatgravyboat.repolib.api.recipes.ingredient.PetIngredient;
import tech.thatgravyboat.repolib.api.recipes.ingredient.PotionIngredient;
import tech.thatgravyboat.repolib.api.recipes.ingredient.RuneIngredient;
import tech.thatgravyboat.repolib.internal.JsonHelper;
import tech.thatgravyboat.repolib.internal.Utils;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public final class IdOverlaysAPI {

    public record WikiData(
            @Deprecated(forRemoval = true) @Nullable String official,
            @Nullable String independent
    ) {
        public static @Nullable WikiData fromJson(JsonObject json) {
            if (json == null) return null;
            return new WikiData(null, JsonHelper.getStringOrNull(json, "independent"));
        }
    }

    public record OverlayData(
            @Nullable WikiData wiki,
            boolean vanilla,
            @Nullable List<Requirement> requirements,
            JsonObject rawObject
    ) {
        public static OverlayData fromJson(JsonObject json) {
            return new OverlayData(
                    json.has("wiki") ? WikiData.fromJson(json.getAsJsonObject("wiki")) : null,
                    JsonHelper.getBoolean(json, "vanilla", false),
                    json.has("requirements") ? JsonHelper.getList(json, "requirements", e -> Requirement.parse(e.getAsJsonObject())) : null,
                    json
            );
        }
    }

    private final Map<String, OverlayData> items = new HashMap<>();
    private final Map<String, OverlayData> potions = new HashMap<>();
    private final Map<String, OverlayData> runes = new HashMap<>();
    private final Map<String, OverlayData> pets = new HashMap<>();
    private final Map<String, OverlayData> mobs = new HashMap<>();
    private final Map<String, OverlayData> enchantments = new HashMap<>();
    private final Map<String, OverlayData> attributes = new HashMap<>();

    void load(JsonElement json) {
        items.clear();
        potions.clear();
        runes.clear();
        pets.clear();
        mobs.clear();
        enchantments.clear();
        attributes.clear();

        if (json instanceof JsonArray array) {
            for (JsonElement element : array) {
                if (element instanceof JsonObject obj) {
                    String type = JsonHelper.getString(obj, "type", "").toLowerCase(Locale.ROOT);
                    String id = JsonHelper.getString(obj, "id", "").toUpperCase(Locale.ROOT);
                    OverlayData data = OverlayData.fromJson(obj);

                    switch (type) {
                        case "item" -> this.items.put(id, data);
                        case "mob" -> this.mobs.put(id, data);
                        case "attribute" -> this.attributes.put(id, data);
                        case "potion" -> {
                            int level = JsonHelper.getInt(obj, "level", 1);
                            this.potions.put(id + ";" + level, data);
                        }
                        case "enchantment" -> {
                            int level = JsonHelper.getInt(obj, "level", 1);
                            this.enchantments.put(id + ";" + level, data);
                        }
                        case "rune" -> {
                            int tier = JsonHelper.getInt(obj, "tier", 1);
                            this.runes.put(id + ";" + tier, data);
                        }
                        case "pet" -> {
                            String tier = JsonHelper.getString(obj, "tier", "").toUpperCase(Locale.ROOT);
                            this.pets.put(id + ";" + tier, data);
                        }
                    }
                }
            }
        } else {
            RepoLibLogger.warn("/Id Overlays/ Failed to load, expected JsonArray but got " + Utils.typeName(json));
        }
    }

    public @Nullable OverlayData getItem(String id) {
        return this.items.get(id.toUpperCase(Locale.ROOT));
    }

    public @Nullable OverlayData getMob(String id) {
        return this.mobs.get(id.toUpperCase(Locale.ROOT));
    }

    public @Nullable OverlayData getAttribute(String id) {
        return this.attributes.get(id.toUpperCase(Locale.ROOT));
    }

    public @Nullable OverlayData getPotion(String id, int level) {
        return this.potions.get(id.toUpperCase(Locale.ROOT) + ";" + level);
    }

    public @Nullable OverlayData getEnchantment(String id, int level) {
        return this.enchantments.get(id.toUpperCase(Locale.ROOT) + ";" + level);
    }

    public @Nullable OverlayData getRune(String id, int tier) {
        return this.runes.get(id.toUpperCase(Locale.ROOT) + ";" + tier);
    }

    public @Nullable OverlayData getPet(String id, String tier) {
        return this.pets.get(id.toUpperCase(Locale.ROOT) + ";" + tier.toUpperCase(Locale.ROOT));
    }

    public @Nullable OverlayData getFromIngredient(CraftingIngredient ingredient) {
        return switch (ingredient) {
            case ItemIngredient i -> getItem(i.id());
            case PetIngredient p -> getPet(p.id(), p.tier());
            case EnchantmentIngredient e -> getEnchantment(e.id(), e.level());
            case RuneIngredient r -> getRune(r.id(), r.tier());
            case AttributeIngredient a -> getAttribute(a.id());
            case PotionIngredient p -> getPotion(p.id(), p.level());
            default -> null;
        };
    }
}
