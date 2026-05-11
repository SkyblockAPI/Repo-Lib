package tech.thatgravyboat.repolib.v2;

import tech.thatgravyboat.repolib.v2.expl.value.Function;
import tech.thatgravyboat.repolib.v2.expl.value.Str;
import tech.thatgravyboat.repolib.v2.expl.value.Value;

public class RarityFunctions {

    public static Function STACK_RARITY = Function.builder((builder) -> {
        builder.arity(0);
        builder.executeArgless(evaluator -> {
            var meta = evaluator.getField("meta");
            var baseRarity = evaluator.getStringOrNull(evaluator.getField(meta, "rarity"));
            if (baseRarity == null) {
                evaluator.error("Item doesn't have a base rarity!");
                return Value.NIL;
            }

            var rarity = SkyblockRarity.fromString(baseRarity);
            if (rarity.isEmpty()) {
                evaluator.error("Unable to convert " + baseRarity + " to rarity!");
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

            return new Str(currentRarity.name());
        });
    });

    private RarityFunctions() {
    }

}
