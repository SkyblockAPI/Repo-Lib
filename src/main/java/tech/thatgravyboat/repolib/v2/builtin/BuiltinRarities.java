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
                evaluator.panic("Item doesn't have a base rarity!");
                return Value.NIL;
            }

            var rarity = SkyblockRarity.fromString(baseRarity);
            if (rarity.isEmpty()) {
                evaluator.panic("Unable to convert " + baseRarity + " to rarity!");
                return Value.NIL;
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
                    evaluator.panic("Provided rarity is not a string!");
                    return Value.NIL;
                }

                var color = SkyblockRarity.color(rarity);

                if (color.isEmpty()) {
                    evaluator.panic("Unknown rarity " + rarity + "!");
                    return Value.NIL;
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
                    evaluator.panic("Provided rarity is not a string!");
                    return Value.NIL;
                }

                var rarityValue = SkyblockRarity.fromString(rarity);

                if (rarityValue.isEmpty()) {
                    evaluator.panic("Unknown rarity " + rarity + "!");
                    return Value.NIL;
                }

                return new StrValue(rarityValue.get().name());
            });
        });

        builder.field("stack", STACK_RARITY);
        builder.field("isRecombobulated", IS_RECOMBOBULATED);

    });

    private BuiltinRarities() {
    }

}
