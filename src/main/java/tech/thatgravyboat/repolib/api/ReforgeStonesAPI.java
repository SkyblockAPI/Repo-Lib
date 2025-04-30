package tech.thatgravyboat.repolib.api;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

public final class ReforgeStonesAPI {
    private final Map<String, ReforgeData> reforgeStones = new HashMap<>();

    static ReforgeStonesAPI load(JsonElement json) {
        ReforgeStonesAPI api = new ReforgeStonesAPI();
        if (json instanceof JsonObject object) {
            for (var entry : object.entrySet()) {
                String id = entry.getKey().toUpperCase(Locale.ROOT);
                ReforgeData data = ReforgeData.fromJson(entry.getValue().getAsJsonObject());
                api.reforgeStones.put(id, data);
            }
        }
        return api;
    }

    public Map<String, ReforgeData> reforgeStones() {
        return this.reforgeStones;
    }

    public ReforgeData getReforgeStone(String id) {
        return this.reforgeStones.get(id.toUpperCase(Locale.ROOT));
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
