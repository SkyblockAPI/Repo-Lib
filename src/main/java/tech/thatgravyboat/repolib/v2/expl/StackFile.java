package tech.thatgravyboat.repolib.v2.expl;

import java.util.function.Function;
import tech.thatgravyboat.repolib.v2.RepoConfig;
import tech.thatgravyboat.repolib.v2.RepoConstants;
import tech.thatgravyboat.repolib.v2.RepoLoader;
import tech.thatgravyboat.repolib.v2.builtin.Constants;
import tech.thatgravyboat.repolib.v2.expl.expression.Expression;
import tech.thatgravyboat.repolib.v2.expl.expression.SelfEvaluatingExpression;
import tech.thatgravyboat.repolib.v2.expl.value.ArrayValue;
import tech.thatgravyboat.repolib.v2.expl.value.FunctionValue;
import tech.thatgravyboat.repolib.v2.expl.value.ImmutableStructValue;
import tech.thatgravyboat.repolib.v2.expl.value.KeyValue;
import tech.thatgravyboat.repolib.v2.expl.value.LambdaFunctionValue;
import tech.thatgravyboat.repolib.v2.expl.value.LayeredStructValue;
import tech.thatgravyboat.repolib.v2.expl.value.MutableArrayValue;
import tech.thatgravyboat.repolib.v2.expl.value.MutableStructValue;
import tech.thatgravyboat.repolib.v2.expl.value.StructValue;
import tech.thatgravyboat.repolib.v2.expl.value.Value;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;

public final class StackFile implements SelfEvaluatingExpression {

    private static final Expression SCRIPT = Expression.parse("include(\"item\");");
    public static final Supplier<Expression> DEFAULT_SCRIPT = () -> SCRIPT;
    private final RepoLoader loader;
    private final Expression script;
    private final Expression metaScript;
    private KeyValue meta;

    public StackFile(RepoLoader loader, Expression meta, Expression script) {
        this.loader = loader;
        this.script = script;
        this.metaScript = meta;
    }

    public void init(RepoConstants constants) {
        var struct = new MutableStructValue();
        struct.set(
                "include", Constants.Builder.FunctionBuilder.create(function -> {
                    function.arity(1);
                    function.execute((evaluator, args) -> {
                        var value = evaluator.getStringOrThrow(args.getFirst());
                        var requested = loader.getModule(value);
                        if (requested == null) {
                            return evaluator.panic("Requested include " + value + " doesn't exist!");
                        }
                        evaluator.pushPop(
                                value, () -> {
                                    evaluator.evaluate(requested);
                                    return Value.NIL;
                                });

                        return Value.NIL;
                    });
                }));
        struct.set(
                "static", Constants.Builder.FunctionBuilder.create(function -> {
                    function.arity(1);
                    function.execute((evaluator, args) -> {
                        var value = evaluator.getStringOrThrow(args.getFirst());
                        var requested = loader.getModule(value);
                        if (requested == null) {
                            return evaluator.panic("Requested include " + value + " doesn't exist!");
                        }
                        if (requested instanceof ModuleFile module) {
                            return module.getStaticData();
                        }

                        return evaluator.panic("Can't access static data of non module file!");
                    });
                }));
        var evaluator = new Evaluator(new LayeredStructValue(struct, constants), loader::getModule);
        evaluator.evaluate(this.metaScript);
        struct.fields().remove("include");
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

    public Evaluator createEvaluator(StructValue overrides, Function<String, FunctionValue> lookup) {
        return createEvaluator(overrides, ImmutableStructValue.EMPTY, RepoConfig.DEFAULT, lookup);
    }

    public Evaluator createEvaluator(StructValue overrides, StructValue data, Function<String, FunctionValue> lookup) {
        return createEvaluator(overrides, data, RepoConfig.DEFAULT, lookup);
    }

    public Evaluator createEvaluator(StructValue overrides, StructValue data, RepoConfig config, Function<String, FunctionValue> lookup) {
        var inputs = new MutableStructValue();

        for (var entry : overrides) {
            inputs.set(entry.getKey(), entry.getValue());
        }

        inputs.set("config", config);

        inputs.set(
                "stack", Constants.mutable((builder) -> {
                    builder.field(
                            "lore", MutableArrayValue.create(entries -> new Constants(lore -> {
                                var section = new AtomicBoolean();

                                lore.function(
                                        "empty", (function) -> {
                                            function.vararg(true);
                                            function.execute(((evaluator, values) -> {
                                                entries.add(ImmutableStructValue.EMPTY);

                                                return Value.NIL;
                                            }));
                                        });
                                lore.function(
                                        "beginSection", (function) -> function.runs(() -> {
                                            if (section.get()) {
                                                entries.add(ImmutableStructValue.EMPTY);
                                            }
                                            section.set(false);
                                        }));
                                lore.function(
                                        "endSection", (function) -> function.runs(() -> {
                                            if (section.get()) {
                                                entries.add(ImmutableStructValue.EMPTY);
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
                                lore.function(
                                        "addAll", function -> {
                                            function.arity(1);
                                            function.executeVoid((evaluator, args) -> {
                                                var values = ArrayValue.flatten(args);
                                                if (values.isEmpty()) {
                                                    return;
                                                }
                                                section.set(true);
                                                entries.addAll(values);
                                            });
                                        });

                            })));
                }).toMutable());
        inputs.set("data", data);
        inputs.set("meta", this.meta);

        return new Evaluator(inputs, lookup);
    }

    public StructValue evaluateScript(Evaluator evaluator) {
        evaluator.evaluate(script);

        return evaluator.defaults.get("stack") instanceof StructValue value ? value : ImmutableStructValue.EMPTY;
    }

    public KeyValue evaluate(StructValue overrides, Function<String, FunctionValue> lookup) {
        return evaluateScript(createEvaluator(overrides, lookup));
    }
}
