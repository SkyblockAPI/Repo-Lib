package tech.thatgravyboat.repolib.api;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public final class EnchantsAPI {

    private final Map<String, JsonObject> enchantments = new HashMap<>();

    static EnchantsAPI load(JsonElement json) {
        EnchantsAPI api = new EnchantsAPI();
        if (json instanceof JsonObject object) {
            object.asMap().forEach((key, value) -> {
                if (value instanceof JsonObject valueObject) {
                    api.enchantments.put(key.toUpperCase(Locale.ROOT), valueObject);
                }
            });
        }
        return api;
    }

    public Map<String, JsonObject> enchantments() {
        return this.enchantments;
    }

    public JsonObject getEnchantment(String id) {
        return this.enchantments.get(id.toUpperCase(Locale.ROOT));
    }

}
