package tech.thatgravyboat.repolib.v2;

import org.jetbrains.annotations.NotNull;
import tech.thatgravyboat.repolib.v2.expl.Value;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;

public final class RepoConstants implements Value.KeyValue {
    private RepoLoader loader;
    private final Map<String, Value> valueMap = new HashMap<>() {
        {
            put(
                    "include", Function.of((evaluator, args) -> {
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
                            evaluator.evaluate(requested);
                        } else {
                            evaluator.error("Expected first argument to be a arg, got " + arg.toString());
                        }
                        return NIL;
                    }));

            put("debug", Function.of((evaluator, args) -> {
                evaluator.debug(args.toString());
                return NIL;
            }));
        }
    };

    public RepoConstants(RepoLoader loader) {
        this.loader = loader;
    }

    @Override
    public @NotNull Iterator<Map.Entry<String, Value>> iterator() {
        return valueMap.entrySet().iterator();
    }

    @Override
    public Value get(String field) {
        return Objects.requireNonNullElse(valueMap.get(field), Value.NIL);
    }

    @Override
    public Mutable toMutable() {
        return new MutableStruct(Map.of());
    }

    public RepoLoader loader() {
        return loader;
    }
}
