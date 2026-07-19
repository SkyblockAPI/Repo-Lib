package tech.thatgravyboat.repolib.v2;

import tech.thatgravyboat.repolib.v2.builtin.BuiltinArray;
import tech.thatgravyboat.repolib.v2.builtin.BuiltinBoolean;
import tech.thatgravyboat.repolib.v2.builtin.BuiltinComponent;
import tech.thatgravyboat.repolib.v2.builtin.BuiltinMath;
import tech.thatgravyboat.repolib.v2.builtin.BuiltinObjects;
import tech.thatgravyboat.repolib.v2.builtin.BuiltinRarities;
import tech.thatgravyboat.repolib.v2.builtin.BuiltinString;
import tech.thatgravyboat.repolib.v2.builtin.Constants;
import tech.thatgravyboat.repolib.v2.expl.value.StructValue;
import tech.thatgravyboat.repolib.v2.expl.value.Value;

public final class RepoConstants implements StructValue.Forwarding {
    private RepoLoader loader;
    private final Constants constants = new Constants((builder) -> {
        builder.field("Math", BuiltinMath.MATH);
        builder.field("Objects", BuiltinObjects.OBJECTS);
        builder.field("Rarity", BuiltinRarities.RARITY);
        builder.field("String", BuiltinString.STRING);
        builder.field("Boolean", BuiltinBoolean.BOOLEAN);
        builder.field("Component", BuiltinComponent.COMPONENT);
        builder.field("Array", BuiltinArray.ARRAY);


        builder.function("include", function -> {
            function.arity(1, 2);
            function.vararg(true);
            function.execute((evaluator, args) -> {
                var value = evaluator.getStringOrThrow(args.getFirst());
                var requested = loader.getModule(value);
                if (requested == null) {
                    return evaluator.panic("Requested include " + value + " doesn't exist!");
                }

                if (args.size() == 2) {
                    var scope = evaluator.getMutableStructOrThrow(args.get(1));
                    return evaluator.pushPop(value, scope, () -> {
                        evaluator.evaluate(requested);
                        return Value.NIL;
                    });
                } else {
                    return evaluator.pushPop(value, () -> {
                        evaluator.evaluate(requested);
                        return Value.NIL;
                    });
                }
            });
        });

        builder.function("call", function -> {
            function.arity(1, 2);
            function.vararg(true);
            function.execute((evaluator, args) -> {
                var value = evaluator.getStringOrThrow(args.getFirst());
                var requested = loader.getModule(value);
                if (requested == null) {
                    return evaluator.panic("Requested include " + value + " doesn't exist!");
                }
                if (args.size() == 2) {
                    var scope = evaluator.getMutableStructOrThrow(args.get(1));
                    return evaluator.pushPop(value, scope, () -> evaluator.evaluate(requested));
                } else {
                    return evaluator.pushPop(value, () -> evaluator.evaluate(requested));
                }
            });
        });

        builder.function("static", function -> {
            function.arity(1);
            function.execute((evaluator, args) -> {
                var value = evaluator.getStringOrThrow(args.getFirst());
                var requested = loader.getModule(value);
                if (requested == null) {
                    return evaluator.panic("Requested include " + value + " doesn't exist!");
                }
                return requested.getStaticData();
            });
        });

        builder.function("debug", (function) -> {
            function.vararg(true);
            function.execute((evaluator, values) -> {
                evaluator.debug(values.toString());
                return NIL;
            });
        });
    });

    public RepoConstants(RepoLoader loader) {
        this.loader = loader;
    }

    @Override
    public StructValue delegate() {
        return constants;
    }

    public RepoLoader loader() {
        return loader;
    }
}
