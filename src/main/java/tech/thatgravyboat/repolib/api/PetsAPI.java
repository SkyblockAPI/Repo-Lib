package tech.thatgravyboat.repolib.api;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.jetbrains.annotations.Nullable;
import tech.thatgravyboat.repolib.api.types.DoubleDoublePair;
import tech.thatgravyboat.repolib.api.types.Pair;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.DoubleUnaryOperator;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public final class PetsAPI {

    private final Map<String, Data> pets = new HashMap<>();
    private final Map<String, Map<String, DoubleUnaryOperator>> petItems = new HashMap<>();

    static PetsAPI load(JsonElement json, JsonObject constants) {
        PetsAPI api = new PetsAPI();
        if (json instanceof JsonObject object) {
            for (var entry : object.entrySet()) {
                api.pets.put(entry.getKey(), Data.fromJson(entry.getValue().getAsJsonObject()));
            }
        }
        if (constants.get("PetItems") instanceof JsonObject object) {
            for (var entry : object.entrySet()) {
                var item = entry.getKey();
                var stats = entry.getValue().getAsJsonObject().getAsJsonObject("pet_stats");
                var operators = stats.entrySet().stream()
                                .map(it -> {
                                    var operator = it.getValue().getAsJsonArray();
                                    var opcode = operator.get(0).getAsString();
                                    var value = operator.get(1).getAsDouble();
                                    return Pair.of(
                                            it.getKey(),
                                            switch (opcode) {
                                                case "+" -> (DoubleUnaryOperator) (x -> x + value);
                                                case "-" -> (DoubleUnaryOperator) (x -> x - value);
                                                case "*" -> (DoubleUnaryOperator) (x -> x * value);
                                                case "/" -> (DoubleUnaryOperator) (x -> x / value);
                                                case "=" -> (DoubleUnaryOperator) (x -> value);
                                                default -> throw new IllegalArgumentException("Unknown opcode: " + opcode);
                                            }
                                    );
                                })
                                .collect(Collectors.toMap(Pair::first, Pair::second));
                api.petItems.put(item.toUpperCase(Locale.ROOT), operators);
            }
        }

        return api;
    }

    public Map<String, Data> pets() {
        return this.pets;
    }

    public Data getPet(String name) {
        return this.pets.get(name.toUpperCase(Locale.ROOT));
    }

    public Map<String, DoubleUnaryOperator> getPetItemStats(String item) {
        return this.petItems.getOrDefault(item, Map.of());
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

            private static final Pattern VARIABLE_PATTERN = Pattern.compile("\\{(?<key>[a-zA-Z0-9_]+)}");

            public double getStat(String key, int level, @Nullable String heldItem) {
                var operators = RepoAPI.pets().getPetItemStats(heldItem);
                var variable = this.variables.get(key);
                var stat = variable.first() + (level / 100.0) * (variable.second() - variable.first());
                var value = Math.floor(stat * 10.0) / 10.0; // round to 1 decimal place
                return operators.getOrDefault(key, x -> x).applyAsDouble(value);
            }

            public double getStat(String key, int level) {
                return this.getStat(key, level, null);
            }

            public List<String> getFormattedLore(int level, @Nullable String heldItem) {
                return this.lore.stream()
                        .map(line -> VARIABLE_PATTERN.matcher(line).replaceAll(match -> {
                            var key = match.group(1);
                            return String.format("%.1f", this.getStat(key, level, heldItem));
                        }))
                        .toList();
            }

            public List<String> getFormattedLore(int level) {
                return getFormattedLore(level, null);
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
