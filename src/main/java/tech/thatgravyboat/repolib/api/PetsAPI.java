package tech.thatgravyboat.repolib.api;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import tech.thatgravyboat.repolib.api.types.DoubleDoublePair;
import tech.thatgravyboat.repolib.api.types.Pair;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public final class PetsAPI {

    private final Map<String, Data> pets = new HashMap<>();

    static PetsAPI load(JsonElement json) {
        PetsAPI api = new PetsAPI();
        if (json instanceof JsonObject object) {
            for (var entry : object.entrySet()) {
                api.pets.put(entry.getKey(), Data.fromJson(entry.getValue().getAsJsonObject()));
            }
        }
        return api;
    }

    public Map<String, Data> pets() {
        return this.pets;
    }

    public Data getPet(String name) {
        return this.pets.get(name);
    }

    public record Data(
            String name,
            Map<String, Tier> tiers
    ) {

        private static Data fromJson(JsonObject json) {
            JsonObject tiers = json.get("tiers").getAsJsonObject();
            return new Data(
                    json.get("name").getAsString(),
                    tiers.entrySet()
                            .stream()
                            .collect(Collectors.toMap(
                                    Map.Entry::getKey,
                                    entry -> Tier.fromJson(entry.getValue().getAsJsonObject())
                            ))
            );
        }

        public record Tier(
                String texture,
                List<String> lore,
                Map<String, DoubleDoublePair> variables
        ) {

            public double getStat(String key, int level) {
                var variable = this.variables.get(key);
                var stat = variable.first() + (level / 100.0) * (variable.second() - variable.first());
                return Math.floor(stat * 10.0) / 10.0; // round to 1 decimal place
            }

            private static Tier fromJson(JsonObject json) {
                JsonArray lore = json.get("lore").getAsJsonArray();
                return new Tier(
                        json.get("texture").getAsString(),
                        IntStream.range(0, lore.size())
                                .mapToObj(lore::get)
                                .map(JsonElement::getAsString)
                                .toList(),
                        json.get("variables").getAsJsonObject().entrySet().stream()
                                .map(entry -> new Pair<>(
                                        entry.getKey(),
                                        new DoubleDoublePair(entry.getValue().getAsJsonArray().get(0).getAsDouble(), entry.getValue().getAsJsonArray().get(1).getAsDouble())
                                ))
                                .collect(Collectors.toMap(Pair::first, Pair::second))
                );
            }
        }
    }
}
