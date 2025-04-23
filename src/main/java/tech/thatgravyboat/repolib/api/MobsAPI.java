package tech.thatgravyboat.repolib.api;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import tech.thatgravyboat.repolib.api.mobs.Mob;
import tech.thatgravyboat.repolib.api.types.Position;

import java.util.HashMap;
import java.util.Map;

public final class MobsAPI {

    private final Map<String, Mob> mobs = new HashMap<>();

    static MobsAPI load(JsonElement json) {
        MobsAPI api = new MobsAPI();
        if (json instanceof JsonObject object) {
            for (var entry : object.entrySet()) {
                String id = entry.getKey();
                JsonObject mobObject = entry.getValue().getAsJsonObject();
                api.mobs.put(id, new Mob(
                        mobObject.has("island") ? mobObject.get("island").getAsString() : null,
                        mobObject.has("position") ? Position.fromJson(mobObject.getAsJsonObject("position")) : null,
                        mobObject.has("texture") ? mobObject.get("texture").getAsString() : null
                ));
            }
        }
        return api;
    }

    public Map<String, Mob> mobs() {
        return this.mobs;
    }

    public Mob getMob(String name) {
        return this.mobs.get(name);
    }
}
