package tech.thatgravyboat.repolib.api;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public final class AttributesAPI {

    private final Map<String, Attribute> attributes = new HashMap<>();

    static AttributesAPI load(JsonElement json) {
        AttributesAPI api = new AttributesAPI();
        if (json instanceof JsonArray array) {
            array.forEach((element) -> {
                if (!(element instanceof JsonObject object)) {
                    return;
                }
                var id = object.get("id");
                if (id == null) {
                    throw new IllegalStateException("Attribute is missing id, item " + object);
                }
                api.attributes.put(id.getAsString().toUpperCase(Locale.ROOT), Attribute.fromJson(object));
            });
        }
        return api;
    }

    public Map<String, Attribute> attributes() {
        return this.attributes;
    }

    public Attribute getAttribute(String id) {
        return this.attributes.get(id.toUpperCase(Locale.ROOT));
    }

    public record Attribute(
            @NotNull String id,
            @NotNull List<String> lore,
            @NotNull String attributeId,
            @NotNull String shardName,
            @NotNull String name,
            @NotNull String item,
            @Nullable String texture,
            @NotNull String rarity,
            int max
    ) {
        public static Attribute fromJson(JsonObject jsonObject) {
            return new Attribute(
                    jsonObject.get("id").getAsString(),
                    jsonObject.getAsJsonArray("lore").asList().stream().map(JsonElement::getAsString).toList(),
                    jsonObject.get("attribute_id").getAsString(),
                    jsonObject.get("shard_name").getAsString(),
                    jsonObject.get("name").getAsString(),
                    jsonObject.get("item").getAsString(),
                    Optional.ofNullable(jsonObject.get("texture")).map(JsonElement::getAsString).orElse(null),
                    jsonObject.get("rarity").getAsString(),
                    jsonObject.get("max").getAsInt()
            );
        }
    }
}
