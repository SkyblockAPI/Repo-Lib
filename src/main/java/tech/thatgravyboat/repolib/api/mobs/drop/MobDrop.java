package tech.thatgravyboat.repolib.api.mobs.drop;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public interface MobDrop {

    String type();

    float chance();
    @Nullable String condition();
    List<String> extraLore();

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
            case "currency" -> CurrencyDrop.fromJson(json);
            default -> UnknownDrop.fromJson(json);
        };
    }
}
