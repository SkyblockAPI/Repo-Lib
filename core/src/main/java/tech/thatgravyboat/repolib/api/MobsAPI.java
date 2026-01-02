package tech.thatgravyboat.repolib.api;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import tech.thatgravyboat.repolib.api.mobs.Mob;
import tech.thatgravyboat.repolib.api.types.Position;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

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
                        mobObject.get("name").getAsString()
                ));
            }
        }
    }

    public Map<String, Mob> mobs() {
        return this.mobs;
    }

    public Mob getMob(String name) {
        return this.mobs.get(name.toUpperCase(Locale.ROOT));
    }
}
