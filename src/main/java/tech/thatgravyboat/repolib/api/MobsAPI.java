package tech.thatgravyboat.repolib.api;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import tech.thatgravyboat.repolib.api.mobs.LootTable;
import tech.thatgravyboat.repolib.api.mobs.Mob;
import tech.thatgravyboat.repolib.api.mobs.drop.MobDrop;
import tech.thatgravyboat.repolib.api.types.Position;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

public final class MobsAPI {

    private final Map<String, Mob> mobs = new HashMap<>();

    void load(JsonElement json) {
        if (json instanceof JsonObject object) {
            for (var entry : object.entrySet()) {
                String id = entry.getKey();
                JsonObject mobObject = entry.getValue().getAsJsonObject();
                this.mobs.put(id.toUpperCase(Locale.ROOT), new Mob(
                        mobObject.has("island") ? mobObject.get("island").getAsString() : null,
                        mobObject.has("position") ? Position.fromJson(mobObject.getAsJsonObject("position")) : null,
                        mobObject.has("texture") ? mobObject.get("texture").getAsString() : null,
                        mobObject.get("itemId").getAsString(),
                        mobObject.get("name").getAsString(),
                        mobObject.has("type") ? mobObject.get("type").getAsString() : null,
                        mobObject.has("lootTables") ?
                        mobObject.getAsJsonArray("lootTables")
                                .asList()
                                .stream()
                                .map(JsonElement::getAsJsonObject)
                                .map(MobsAPI::loadLootTable)
                                .collect(Collectors.toList()) : List.of()
                ));
            }
        }
    }

    private static LootTable loadLootTable(JsonObject json) {
        return new LootTable(
                json.get("name").getAsString(),
                json.get("mobLevel").getAsInt(),
                json.get("coins").getAsInt(),
                json.get("xp").getAsInt(),
                json.get("combatXp").getAsInt(),
                json.getAsJsonArray("drops")
                        .asList()
                        .stream()
                        .map(JsonElement::getAsJsonObject)
                        .map(MobDrop::parse)
                        .collect(Collectors.toList())
        );
    }

    public Map<String, Mob> mobs() {
        return this.mobs;
    }

    public Mob getMob(String name) {
        return this.mobs.get(name.toUpperCase(Locale.ROOT));
    }
}
