package tech.thatgravyboat.repolib.v2;

import org.jetbrains.annotations.NotNull;
import tech.thatgravyboat.repolib.v2.expl.value.KeyValue;
import tech.thatgravyboat.repolib.v2.expl.value.Str;
import tech.thatgravyboat.repolib.v2.expl.value.Value;

import java.util.Iterator;
import java.util.Map;

public final class RepoConstants implements KeyValue.Forwarding {
    private RepoLoader loader;
    private final Constants constants = new Constants((builder) -> {

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
    public @NotNull Iterator<Map.Entry<String, Value>> iterator() {
        return constants.iterator();
    }

    @Override
    public KeyValue delegate() {
        return constants;
    }

    public RepoLoader loader() {
        return loader;
    }
}
