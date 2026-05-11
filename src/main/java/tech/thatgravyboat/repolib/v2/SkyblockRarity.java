package tech.thatgravyboat.repolib.v2;

import java.util.Optional;

public enum SkyblockRarity {
    COMMON("WHITE"),
    UNCOMMON("GREEN"),
    RARE("BLUE"),
    EPIC("DARK_PURPLE"),
    LEGENDARY("GOLD"),
    MYTHIC("LIGHT_PURPLE"),
    DIVINE("AQUA"),
    ULTIMATE("DARK_RED"),
    SPECIAL("RED"),
    VERY_SPECIAL("RED"),
    ADMIN("RED"),
    ;

    private final String color;

    SkyblockRarity(String color) {
        this.color = color;
    }

    public String color() {
        return color;
    }

    public SkyblockRarity next() {
        return switch (this) {
            case COMMON -> UNCOMMON;
            case UNCOMMON -> RARE;
            case RARE -> EPIC;
            case EPIC -> LEGENDARY;
            case LEGENDARY -> MYTHIC;
            case MYTHIC -> DIVINE;
            case DIVINE -> ULTIMATE;
            case ULTIMATE -> SPECIAL;
            case SPECIAL -> VERY_SPECIAL;
            default -> this;
        };
    }

    public static Optional<SkyblockRarity> next(String current) {
        return fromString(current).map(SkyblockRarity::next);
    }

    public static Optional<SkyblockRarity> fromString(String rarity) {
        try {
            return Optional.of(SkyblockRarity.valueOf(rarity));
        } catch (IllegalArgumentException ignored) {
            return Optional.empty();
        }
    }

    public static Optional<String> color(String rarity) {
        return fromString(rarity).map(SkyblockRarity::color);
    }
}
