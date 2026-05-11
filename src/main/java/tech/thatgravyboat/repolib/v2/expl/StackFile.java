package tech.thatgravyboat.repolib.v2.expl;

import tech.thatgravyboat.repolib.v2.builtin.BuiltinRarities;
import tech.thatgravyboat.repolib.v2.builtin.Constants;
import tech.thatgravyboat.repolib.v2.expl.expression.Expression;
import tech.thatgravyboat.repolib.v2.expl.expression.SelfEvaluatingExpression;
import tech.thatgravyboat.repolib.v2.expl.value.Array;
import tech.thatgravyboat.repolib.v2.expl.value.ImmutableStruct;
import tech.thatgravyboat.repolib.v2.expl.value.KeyValue;
import tech.thatgravyboat.repolib.v2.expl.value.MutableArray;
import tech.thatgravyboat.repolib.v2.expl.value.MutableStruct;
import tech.thatgravyboat.repolib.v2.expl.value.Struct;
import tech.thatgravyboat.repolib.v2.expl.value.Value;

import java.util.concurrent.atomic.AtomicBoolean;

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

    public KeyValue meta() {
        return meta;
    }

    public Evaluator createEvaluator(Struct overrides) {
        return createEvaluator(overrides, ImmutableStruct.EMPTY);
    }

    public Evaluator createEvaluator(Struct overrides, Struct data) {
        var inputs = new MutableStruct();

        for (var entry : overrides) {
            inputs.set(entry.getKey(), entry.getValue());
        }

        inputs.set(
                "stack", Constants.mutable((builder) -> {
                    builder.field(
                            "lore", MutableArray.create(entries -> new Constants(lore -> {
                                var section = new AtomicBoolean();

                                lore.function(
                                        "empty", (function) -> {
                                            function.vararg(true);
                                            function.execute(((evaluator, values) -> {
                                                entries.add(ImmutableStruct.EMPTY);

                                                return Value.NIL;
                                            }));
                                        });
                                lore.function(
                                        "beginSection", (function) -> function.runs(() -> {
                                            if (section.get()) {
                                                entries.add(ImmutableStruct.EMPTY);
                                            }
                                            section.set(false);
                                        }));
                                lore.function(
                                        "endSection", (function) -> function.runs(() -> {
                                            if (section.get()) {
                                                entries.add(ImmutableStruct.EMPTY);
                                            }
                                            section.set(false);
                                        }));
                                lore.function("clear", (function) -> function.runs(entries::clear));
                                lore.function(
                                        "add", function -> {
                                            function.arity(1);
                                            function.executeSimpleVoid(args -> {
                                                section.set(true);
                                                entries.add(args.getFirst());
                                            });
                                        });
                                lore.function("addAll", function -> {
                                    function.arity(1);
                                    function.executeVoid((evaluator, args) -> {
                                        var values = Array.flatten(args);
                                        if (values.isEmpty()) return;
                                        section.set(true);
                                        entries.addAll(values);
                                    });
                                });

                            })));
                }).toMutable());
        inputs.set("data", data);
        inputs.set("meta", this.meta);

        return new Evaluator(inputs);
    }

    public Struct evaluateScript(Evaluator evaluator) {
        evaluator.evaluate(script);

        return evaluator.defaults.get("stack") instanceof Struct value ? value : ImmutableStruct.EMPTY;
    }

    public KeyValue evaluate(Struct overrides) {
        return evaluateScript(createEvaluator(overrides));
    }
}
