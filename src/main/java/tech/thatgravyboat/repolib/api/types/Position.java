package tech.thatgravyboat.repolib.api.types;

import com.google.gson.JsonObject;
import org.jetbrains.annotations.ApiStatus;

public record Position(
        int x,
        int y,
        int z
) {

    @ApiStatus.Internal
    public static Position fromJson(JsonObject object) {
        return new Position(
                object.get("x").getAsInt(),
                object.get("y").getAsInt(),
                object.get("z").getAsInt()
        );
    }
}
