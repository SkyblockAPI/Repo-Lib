package tech.thatgravyboat.repolib.api;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.sql.Ref;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

public final class ReforgeStonesAPI {
    private final Map<String, ReforgeData> reforgeStones = new HashMap<>();

    static ReforgeStonesAPI load(JsonElement json) {
        ReforgeStonesAPI api = new ReforgeStonesAPI();
        if (json instanceof JsonObject object) {
            object.entrySet().stream().map(Map.Entry::getValue).forEach(element -> {
                if (element instanceof JsonObject jsonObject) {
                    ReforgeData reforge = ReforgeData.fromJson(jsonObject);
                    api.reforgeStones.put(reforge.name().toUpperCase(Locale.ROOT), reforge);
                }
            });
        }
        return api;
    }

    public Map<String, ReforgeData> reforgeStones() {
        return this.reforgeStones;
    }

    public ReforgeData getReforgeStone(String name) {
        return this.reforgeStones.get(name.toUpperCase(Locale.ROOT));
    }

    public record ReforgeData(
            String name,
            Map<String, Long> applyCost,
            Map<String, Map<String, Integer>> stats
    ) {
        private static ReforgeData fromJson(JsonObject json) {
            Map<String, Long> applyCost = json.getAsJsonObject("reforgeCosts").entrySet()
                    .stream()
                    .collect(Collectors.toMap(
                            Map.Entry::getKey,
                            entry -> entry.getValue().getAsLong()
                    ));
            Map<String, Map<String, Integer>> stats = json.getAsJsonObject("reforgeStats").entrySet()
                    .stream()
                    .collect(Collectors.toMap(
                            Map.Entry::getKey,
                            entry -> {
                                JsonObject stat = entry.getValue().getAsJsonObject();
                                return stat.entrySet()
                                        .stream()
                                        .collect(Collectors.toMap(
                                                Map.Entry::getKey,
                                                e -> e.getValue().getAsInt()
                                        ));
                            }
                    ));

            return new ReforgeData(
                    json.get("reforgeName").getAsString(),
                    applyCost,
                    stats
            );
        }
    }
}
