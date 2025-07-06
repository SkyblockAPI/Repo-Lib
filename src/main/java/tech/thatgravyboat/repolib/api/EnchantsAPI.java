package tech.thatgravyboat.repolib.api;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public final class EnchantsAPI {

    private final Map<String, Enchant> enchantments = new HashMap<>();

    static EnchantsAPI load(JsonElement json) {
        EnchantsAPI api = new EnchantsAPI();
        if (json instanceof JsonObject object) {
            object.asMap().forEach((key, value) -> {
                if (value instanceof JsonObject valueObject) {
                    api.enchantments.put(key.toUpperCase(Locale.ROOT), Enchant.fromJson(valueObject));
                }
            });
        }
        return api;
    }

    public Map<String, Enchant> enchantments() {
        return this.enchantments;
    }

    public Enchant getEnchantment(String id) {
        return this.enchantments.get(id.toUpperCase(Locale.ROOT));
    }

    public record Enchant(
            @NotNull String id,
            @NotNull String name,
            boolean isUltimate,
            @NotNull Map<Integer, EnchantLevel> levels
    ) {
        public static Enchant fromJson(JsonObject object) {
            try {
                return new Enchant(
                        object.get("id").getAsString(),
                        object.get("name").getAsString(),
                        object.get("isUltimate").getAsBoolean(),
                        object.getAsJsonArray("levels")
                                .asList()
                                .stream()
                                .map(JsonElement::getAsJsonObject)
                                .map(EnchantLevel::fromJson)
                                .collect(Collectors.toMap(EnchantLevel::level, Function.identity()))
                );
            } catch (Exception e) {
                throw new IllegalArgumentException("Invalid Enchant JSON: " + object, e);
            }
        }
    }

    public record EnchantLevel(
            int level,
            @NotNull String literalLevel,
            @NotNull List<String> lore
    ) {
        public static EnchantLevel fromJson(JsonObject object) {
            return new EnchantLevel(
                    object.get("level").getAsInt(),
                    object.get("literal_level").getAsString(),
                    object.getAsJsonArray("lore").asList().stream().map(JsonElement::getAsString).toList()
            );
        }
    }
}
