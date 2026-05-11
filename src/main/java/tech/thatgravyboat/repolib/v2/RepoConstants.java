package tech.thatgravyboat.repolib.v2;

import tech.thatgravyboat.repolib.v2.builtin.BuiltinMath;
import tech.thatgravyboat.repolib.v2.builtin.BuiltinObjects;
import tech.thatgravyboat.repolib.v2.builtin.BuiltinRarities;
import tech.thatgravyboat.repolib.v2.builtin.BuiltinString;
import tech.thatgravyboat.repolib.v2.builtin.Constants;
import tech.thatgravyboat.repolib.v2.expl.value.Str;
import tech.thatgravyboat.repolib.v2.expl.value.Struct;

public final class RepoConstants implements Struct.Forwarding {
    private RepoLoader loader;
    private final Constants constants = new Constants((builder) -> {
        builder.field("Math", BuiltinMath.MATH);
        builder.field("Objects", BuiltinObjects.OBJECTS);
        builder.field("Rarity", BuiltinRarities.RARITY);
        builder.field("String", BuiltinString.STRING);

        builder.function("include", (function) -> {
            function.arity(1);
            function.execute((evaluator, args) -> {
                if (args.size() != 1) {
                    evaluator.error("Expected 1 argument for include, got " + args.size());
                    return NIL;
                }
                var arg = args.getFirst();
                if (arg instanceof Str(String value)) {
                    var requested = loader.getExpression(value);
                    if (requested == null) {
                        evaluator.error("Requested include " + value + " doesn't exist!");
                        return NIL;
                    }
                    evaluator.pushPop(value, () -> {
                        evaluator.evaluate(requested);
                        return NIL;
                    });
                } else {
                    evaluator.error("Expected first argument to be a arg, got " + arg.toString());
                }
                return NIL;
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
    public Struct delegate() {
        return constants;
    }

    public RepoLoader loader() {
        return loader;
    }
}
