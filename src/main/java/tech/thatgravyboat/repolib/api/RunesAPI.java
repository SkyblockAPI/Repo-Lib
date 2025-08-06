package tech.thatgravyboat.repolib.api;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class RunesAPI {

    private final Map<String, List<Rune>> runes = new HashMap<>();

    public Map<String, List<Rune>> runes() {
        return this.runes;
    }

    public List<Rune> getRunes(String id) {
        return this.runes.get(id.toUpperCase(Locale.ROOT));
    }

    void load(JsonObject json) {
        for (var entry : json.entrySet()) {
            String id = entry.getKey().toUpperCase(Locale.ROOT);
            List<Rune> data = entry.getValue().getAsJsonArray().asList().stream()
                    .map(JsonElement::getAsJsonObject)
                    .map($1 -> Rune.fromJson(id, $1))
                    .toList();
            this.runes.put(id, data);
        }
    }

    public record Rune(
            String id,
            int tier,
            String texture,
            String name,
            List<String> lore
    ) {
        static Rune fromJson(String id, JsonObject json) {
            return new Rune(
                    id,
                    json.get("tier").getAsInt(),
                    json.get("texture").getAsString(),
                    json.get("name").getAsString(),
                    json.getAsJsonArray("lore").asList()
                            .stream()
                            .map(JsonElement::getAsString)
                            .toList()
            );
        }
    }
}
