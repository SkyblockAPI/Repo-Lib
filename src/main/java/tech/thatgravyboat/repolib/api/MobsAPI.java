package tech.thatgravyboat.repolib.api;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import tech.thatgravyboat.repolib.api.mobs.LootTable;
import tech.thatgravyboat.repolib.api.mobs.Mob;
import tech.thatgravyboat.repolib.api.mobs.drop.MobDrop;
import tech.thatgravyboat.repolib.api.types.Position;
import tech.thatgravyboat.repolib.internal.JsonHelper;

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
                        JsonHelper.getStringOrNull(mobObject, "island"),
                        mobObject.has("position") ? Position.fromJson(mobObject.getAsJsonObject("position")) : null,
                        JsonHelper.getStringOrNull(mobObject, "texture"),
                        mobObject.get("itemId").getAsString(),
                        mobObject.get("name").getAsString(),
                        JsonHelper.getStringOrNull(mobObject, "type"),
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
                JsonHelper.getInt(json, "mobLevel", 0),
                JsonHelper.getInt(json, "coins", 0),
                JsonHelper.getInt(json, "xp", 0),
                JsonHelper.getInt(json, "combatXp", 0),
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
