package tech.thatgravyboat.repolib.v2.expl;

import tech.thatgravyboat.repolib.v2.RepoConfig;
import tech.thatgravyboat.repolib.v2.RepoConstants;
import tech.thatgravyboat.repolib.v2.RepoLoader;
import tech.thatgravyboat.repolib.v2.binary.BinaryCodec;
import tech.thatgravyboat.repolib.v2.binary.BinaryFileTypeRegistry;
import tech.thatgravyboat.repolib.v2.binary.BinaryRecordBuilder;
import tech.thatgravyboat.repolib.v2.binary.ExpressionTypeRegistry;
import tech.thatgravyboat.repolib.v2.binary.FileTypes;
import tech.thatgravyboat.repolib.v2.binary.TypedFile;
import tech.thatgravyboat.repolib.v2.builtin.Constants;
import tech.thatgravyboat.repolib.v2.expl.expression.Expression;
import tech.thatgravyboat.repolib.v2.expl.expression.SelfEvaluatingExpression;
import tech.thatgravyboat.repolib.v2.expl.value.ArrayValue;
import tech.thatgravyboat.repolib.v2.expl.value.FunctionValue;
import tech.thatgravyboat.repolib.v2.expl.value.ImmutableStructValue;
import tech.thatgravyboat.repolib.v2.expl.value.KeyValue;
import tech.thatgravyboat.repolib.v2.expl.value.LayeredStructValue;
import tech.thatgravyboat.repolib.v2.expl.value.MutableArrayValue;
import tech.thatgravyboat.repolib.v2.expl.value.MutableStructValue;
import tech.thatgravyboat.repolib.v2.expl.value.StructValue;
import tech.thatgravyboat.repolib.v2.expl.value.Value;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;
import java.util.function.Supplier;

public final class StackFile implements SelfEvaluatingExpression<StackFile>, TypedFile<StackFile> {

    public static final BinaryCodec<StackFile> CODEC = BinaryRecordBuilder.of(
        BinaryCodec.STRING.forGetter(StackFile::name),
        BinaryCodec.NULLABLE_EXPRESSION.forGetter(StackFile::metaScript),
        BinaryCodec.NULLABLE_EXPRESSION.forGetter(StackFile::script),
        StackFile::new
    );

    private static final Expression<?> SCRIPT = Expression.parse("include(\"item\");");
    public static final Supplier<Expression<?>> DEFAULT_SCRIPT = () -> SCRIPT;
    private final String name;
    private final Expression<?> script;
    private final Expression<?> metaScript;
    private KeyValue meta;
    private boolean isInitializing = false;

    public StackFile(String name, Expression<?> meta, Expression<?> script) {
        this.name = name;
        this.script = script;
        this.metaScript = meta;
    }

    public boolean needsInitialization() {
        return meta == null;
    }

    public void init(RepoLoader loader, RepoConstants constants) {
        if (this.isInitializing) {
            throw new IllegalStateException("Stack is currently being initialized!");
        }
        this.isInitializing = true;

        var struct = new MutableStructValue();
        struct.set(
            "include", Constants.Builder.FunctionBuilder.create(function -> {
                function.arity(1);
                function.execute((evaluator, args) -> {
                    var value = evaluator.getStringOrThrow(args.getFirst());
                    var requested = loader.module(value);
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
                    var requested = loader.module(value);
                    if (requested == null) {
                        return evaluator.panic("Requested include " + value + " doesn't exist!");
                    }
                    if (requested instanceof ModuleFile module) {
                        return module.staticData();
                    }

                    return evaluator.panic("Can't access static data of non module file!");
                });
            }));
        struct.set("categories", MutableArrayValue.create());
        var evaluator = new Evaluator(new LayeredStructValue(struct, constants), loader::module);
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

    public String name() {
        return name;
    }

    private Expression<?> metaScript() {
        return metaScript;
    }
    private Expression<?> script() {
        return script;
    }

    public Evaluator createEvaluator(StructValue overrides, Function<String, FunctionValue> lookup) {
        return createEvaluator(overrides, ImmutableStructValue.EMPTY, RepoConfig.DEFAULT, lookup);
    }

    public Evaluator createEvaluator(StructValue overrides, StructValue data, Function<String, FunctionValue> lookup) {
        return createEvaluator(overrides, data, RepoConfig.DEFAULT, lookup);
    }

    public Evaluator createEvaluator(
        StructValue overrides,
        StructValue data,
        RepoConfig config,
        Function<String, FunctionValue> lookup
    ) {
        return this.createEvaluator(overrides, data, ImmutableStructValue.EMPTY, config, lookup);
    }

    public Evaluator createEvaluator(
        StructValue overrides,
        StructValue data,
        StructValue profile,
        RepoConfig config,
        Function<String, FunctionValue> lookup
    ) {
        var inputs = new MutableStructValue();

        for (var entry : overrides) {
            inputs.set(entry.getKey(), entry.getValue());
        }

        inputs.set("config", config);

        inputs.set(
            "stack", Constants.mutable((builder) -> builder.field(
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

                })))).toMutable());
        inputs.set("data", data);
        inputs.set("categories", MutableArrayValue.create());
        inputs.set("profile", profile);
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

    @Override
    public BinaryFileTypeRegistry.Type<StackFile> fileId() {
        return FileTypes.STACK;
    }

    @Override
    public ExpressionTypeRegistry.Type<StackFile> expressionId() {
        throw new UnsupportedOperationException();
    }
}
