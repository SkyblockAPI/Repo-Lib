package tech.thatgravyboat.repolib.api;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public final class ItemsAPI {

    private final Map<String, JsonObject> items = new HashMap<>();

    void load(JsonElement json) {
        if (json instanceof JsonArray array) {
            for (var element : array) {
                JsonObject object = element.getAsJsonObject();
                JsonObject components = object.getAsJsonObject("components");
                JsonObject customData = components.getAsJsonObject("minecraft:custom_data");
                JsonElement id = customData.get("id");
                if (id == null) throw new IllegalStateException("Item is missing id, item " + object);
                this.items.put(id.getAsString().toUpperCase(Locale.ROOT), object);
            }
        }
    }

    public Map<String, JsonObject> items() {
        return this.items;
    }

    public JsonObject getItem(String name) {
        return this.items.get(name.toUpperCase(Locale.ROOT));
    }
}
