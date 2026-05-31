package tech.thatgravyboat.repolib.api.mobs.drop;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.jetbrains.annotations.ApiStatus;

public interface MobDrop {

    String type();

    float chance();

    int minAmount();
    int maxAmount();

    @ApiStatus.Internal
    static MobDrop parse(JsonObject json) {
        JsonElement type = json.get("type");
        return switch (type == null ? "item" : type.getAsString()) {
            case "pet" -> PetDrop.fromJson(json);
            case "item" -> ItemDrop.fromJson(json);
            case "enchantment" -> EnchantmentDrop.fromJson(json);
            case "rune" -> RuneDrop.fromJson(json);
            case "attribute" -> AttributeDrop.fromJson(json);
            case "potion" -> PotionDrop.fromJson(json);
            default -> UnknownDrop.fromJson(json);
        };
    }
}
