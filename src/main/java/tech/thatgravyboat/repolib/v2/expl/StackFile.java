package tech.thatgravyboat.repolib.v2.expl;

import tech.thatgravyboat.repolib.v2.Constants;
import tech.thatgravyboat.repolib.v2.SkyblockRarity;
import tech.thatgravyboat.repolib.v2.expl.expression.Expression;
import tech.thatgravyboat.repolib.v2.expl.expression.SelfEvaluatingExpression;
import tech.thatgravyboat.repolib.v2.expl.value.ImmutableStruct;
import tech.thatgravyboat.repolib.v2.expl.value.KeyValue;
import tech.thatgravyboat.repolib.v2.expl.value.MutableStruct;
import tech.thatgravyboat.repolib.v2.expl.value.Str;
import tech.thatgravyboat.repolib.v2.expl.value.Value;

public final class StackFile implements SelfEvaluatingExpression {

    private final Expression script;
    private final KeyValue meta;

    public StackFile(Expression meta, Expression script) {
        this.script = script;

        var struct = new MutableStruct();
        //struct.set("this", struct); // Should allow for access of the top level by also using "this" in the script.
        var evaluator = new Evaluator(struct);
        evaluator.evaluate(meta);
        this.meta = struct.toFullyImmutable();
    }

    @Override
    public Value evaluate(Evaluator evaluator) {
        evaluateScript(evaluator);
        return Value.NIL;
    }

    public Evaluator createEvaluator(KeyValue overrides) {
        return createEvaluator(overrides, ImmutableStruct.EMPTY);
    }

    public Evaluator createEvaluator(KeyValue overrides, KeyValue data) {
        var inputs = new MutableStruct();

        for (var entry : overrides) {
            inputs.set(entry.getKey(), entry.getValue());
        }

        inputs.set("stack", Constants.mutable((builder) -> {
            builder.function("rarity", (function) -> {
                function.arity(0);
                function.executArgless(evaluator -> {
                    var baseRarity = evaluator.getStringOrNull(meta.get("rarity"));
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

                    var rarityUpgrades = evaluator.getNumber(data.get("rarity_upgrades"), 0);
                    if (rarityUpgrades > 0) {
                        while (rarityUpgrades > 0) {
                            currentRarity = currentRarity.next();
                            rarityUpgrades--;
                        }
                    }

                    if (evaluator.getNumber(data.get("baseStatBoostPercentage"), 0) >= 50) {
                        currentRarity = currentRarity.next();
                    }

                    return new Str(currentRarity.name());
                });
            });

            builder.immutableStruct("lore", (lore) -> {
                lore.function("empty", (function) -> {
                    function.vararg(true);
                    function.execute(((evaluator, values) -> {

                        return Value.NIL;
                    }));
                });
                lore.function("beginSection", (function) -> {
                    function.vararg(true);
                    function.execute(((evaluator, values) -> {

                        return Value.NIL;
                    }));
                });
                lore.function("endSection", (function) -> {
                    function.vararg(true);
                    function.execute(((evaluator, values) -> {

                        return Value.NIL;
                    }));
                });
                lore.function("clear", (function) -> {
                    function.vararg(true);
                    function.execute(((evaluator, values) -> {

                        return Value.NIL;
                    }));
                });
                lore.function("add", (function) -> {
                    function.vararg(true);
                    function.execute(((evaluator, values) -> {

                        return Value.NIL;
                    }));
                });
            });
        }).toMutable());
        inputs.set("data", data);
        inputs.set("meta", this.meta);

        return new Evaluator(inputs);
    }

    public KeyValue evaluateScript(Evaluator evaluator) {
        evaluator.evaluate(script);

        return evaluator.defaults.get("stack") instanceof KeyValue value ? value : ImmutableStruct.EMPTY;
    }

    public KeyValue evaluate(KeyValue overrides) {
        return evaluateScript(createEvaluator(overrides));
    }
}
