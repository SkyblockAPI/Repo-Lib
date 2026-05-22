package tech.thatgravyboat.repolib.v2.builtin;

import tech.thatgravyboat.repolib.v2.SkyblockRarity;
import tech.thatgravyboat.repolib.v2.expl.value.BoolValue;
import tech.thatgravyboat.repolib.v2.expl.value.FunctionValue;
import tech.thatgravyboat.repolib.v2.expl.value.StrValue;
import tech.thatgravyboat.repolib.v2.expl.value.Value;

public class BuiltinRarities {


    public static FunctionValue STACK_RARITY = FunctionValue.builder((builder) -> {
        builder.arity(0);
        builder.executeArgless(evaluator -> {
            var meta = evaluator.getField("meta");
            var baseRarity = evaluator.getStringOrNull(evaluator.getField(meta, "rarity"));
            if (baseRarity == null) {
                return evaluator.panic("Item doesn't have a base rarity!");
            }

            var rarity = SkyblockRarity.fromString(baseRarity);
            if (rarity.isEmpty()) {
                return evaluator.panic("Unable to convert " + baseRarity + " to rarity!");
            }

            var currentRarity = rarity.get();

            var data = evaluator.getField("data");

            var rarityUpgrades = evaluator.getNumber(evaluator.getField(data, "rarity_upgrades"), 0);
            if (rarityUpgrades > 0) {
                while (rarityUpgrades > 0) {
                    currentRarity = currentRarity.next();
                    rarityUpgrades--;
                }
            }

            if (evaluator.getNumber(evaluator.getField(data, "baseStatBoostPercentage"), 0) >= 50) {
                currentRarity = currentRarity.next();
            }

            return new StrValue(currentRarity.name());
        });
    });

    public static FunctionValue IS_RECOMBOBULATED = FunctionValue.builder((builder) -> {
        builder.arity(0);
        builder.executeArgless(evaluator -> {
            var data = evaluator.getField("data");

            var rarityUpgrades = evaluator.getNumber(evaluator.getField(data, "rarity_upgrades"), 0);
            if (rarityUpgrades > 0) {
                return BoolValue.TRUE;
            }

            return BoolValue.FALSE;
        });
    });

    public static final Constants RARITY = new Constants(builder -> {

        builder.function("color", function -> {
            function.arity(1);
            function.execute((evaluator, values) -> {
                var first = values.getFirst();
                var rarity = evaluator.getStringOrNull(first);
                if (rarity == null) {
                    return evaluator.panic("Provided rarity is not a string!");
                }

                var color = SkyblockRarity.color(rarity);

                if (color.isEmpty()) {
                    return evaluator.panic("Unknown rarity " + rarity + "!");
                }

                return new StrValue(color.get());
            });
        });
        builder.function("name", function -> {
            function.arity(1);
            function.execute((evaluator, values) -> {
                var first = values.getFirst();
                var rarity = evaluator.getStringOrNull(first);
                if (rarity == null) {
                    return evaluator.panic("Provided rarity is not a string!");
                }

                var rarityValue = SkyblockRarity.fromString(rarity);

                if (rarityValue.isEmpty()) {
                    return evaluator.panic("Unknown rarity " + rarity + "!");
                }

                return new StrValue(rarityValue.get().name());
            });
        });

        builder.field("stack", STACK_RARITY);
        builder.field("isRecombobulated", IS_RECOMBOBULATED);

        builder.function("upgrade", function -> {
            function.arity(2);
            function.execute((evaluator, values) -> {
                var first = values.getFirst();
                var rarity = evaluator.getStringOrThrow(first);
                var rarityValue = SkyblockRarity.fromString(rarity);

                if (rarityValue.isEmpty()) {
                    return evaluator.panic("Unknown rarity " + rarity + "!");
                }

                var rarityResult = rarityValue.get();
                var upgrade = (int) evaluator.getNumberOrThrow(values.get(1));
                for (int i = 0; i < upgrade; i++) {
                    rarityResult = rarityResult.next();
                }

                return new StrValue(rarityResult.name());
            });
        });
    });

    private BuiltinRarities() {
    }

}
