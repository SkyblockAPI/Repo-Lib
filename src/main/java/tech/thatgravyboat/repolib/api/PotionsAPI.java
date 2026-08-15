package tech.thatgravyboat.repolib.api;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import tech.thatgravyboat.repolib.internal.JsonHelper;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public final class PotionsAPI {

    private final Map<String, Potion> potions = new HashMap<>();

    void load(JsonElement json) {
        if (json instanceof JsonObject object) {
            object.asMap().forEach((key, value) -> {
                if (value instanceof JsonObject valueObject) {
                    this.potions.put(key.toUpperCase(Locale.ROOT), Potion.fromJson(key, valueObject));
                }
            });
        }
    }

    public Map<String, Potion> potions() {
        return this.potions;
    }

    public Potion getPotion(String id) {
        var potion = this.potions.get(id.toUpperCase(Locale.ROOT));
        if (potion != null) return potion;

        return this.potions.get("POTION_" + id.toUpperCase(Locale.ROOT));
    }

    public @Nullable IdOverlaysAPI.OverlayData getOverlay(String id, int level) {
        return RepoAPI.overlays().getPotion(id, level);
    }

    public record Potion(
            @NotNull String id,
            @NotNull Map<Integer, PotionLevel> levels,
            @NotNull String name,
            @Nullable String type,
            @Nullable String internalPotion,
            @NotNull String vanillaEffect
    ) {
        public static Potion fromJson(String id, JsonObject object) {
            try {
                return new Potion(
                        id,
                        object.getAsJsonArray("levels")
                                .asList()
                                .stream()
                                .map(JsonElement::getAsJsonObject)
                                .map(PotionLevel::fromJson)
                                .collect(Collectors.toMap(PotionLevel::level, Function.identity())),
                        object.get("name").getAsString(),
                        JsonHelper.getString(object, "type", null),
                        JsonHelper.getString(object, "internal_potion", null),
                        object.get("vanilla_effect").getAsString()
                );
            } catch (Exception e) {
                throw new IllegalArgumentException("Invalid Potion JSON: " + object, e);
            }
        }
    }

    public record PotionLevel(
            int level,
            @NotNull String literalLevel,
            @Deprecated @NotNull List<String> lore,
            boolean splash,
            @NotNull JsonObject item
    ) {
        public static PotionLevel fromJson(JsonObject object) {
            return new PotionLevel(
                    JsonHelper.getInt(object, "level", 1),
                    JsonHelper.getString(object, "literal_level", "I"),
                    object.getAsJsonArray("lore").asList().stream().map(JsonElement::getAsString).toList(),
                    JsonHelper.getBoolean(object, "splash", false),
                    object.getAsJsonObject("item")
            );
        }
    }
}
