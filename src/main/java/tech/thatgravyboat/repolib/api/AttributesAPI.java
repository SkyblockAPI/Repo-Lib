package tech.thatgravyboat.repolib.api;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public final class AttributesAPI {

    private final Map<String, JsonObject> attributes = new HashMap<>();

    static AttributesAPI load(JsonElement json) {
        AttributesAPI api = new AttributesAPI();
        if (json instanceof JsonArray array) {
            array.forEach((element) -> {
                if (!(element instanceof JsonObject object)) return;
                var id = object.get("id");
                if (id == null) throw new IllegalStateException("Attribute is missing id, item " + object);
                api.attributes.put(id.getAsString().toUpperCase(Locale.ROOT), object);
            });
        }
        return api;
    }

    public Map<String, JsonObject> attributes() {
        return this.attributes;
    }

    public JsonObject getAttributes(String id) {
        return this.attributes.get(id.toUpperCase(Locale.ROOT));
    }


}
