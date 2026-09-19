package tech.thatgravyboat.repolib.v2;

import tech.thatgravyboat.repolib.v2.builtin.Constants;
import tech.thatgravyboat.repolib.v2.expl.value.MutableArrayValue;
import tech.thatgravyboat.repolib.v2.expl.value.StructValue;

public class RepoListConstants implements StructValue.Forwarding {
    private final RepoLoader loader;
    private final Constants constants;

    public RepoListConstants(RepoConstants repoConstants, RepoLoader loader) {
        this.loader = loader;
        this.constants = new Constants((builder) -> {
            for (var repoConstant : repoConstants) {
                builder.field(repoConstant.getKey(), repoConstant.getValue());
            }

            builder.function("list", function -> {
                function.arity(1);
                function.execute((evaluator, values) -> {
                    var array = MutableArrayValue.create();

                    var prefix = evaluator.getStringOrThrow(values.getFirst());
                    for (var stackEntry : loader.stackFiles().entrySet()) {
                        if (stackEntry.getKey().startsWith(prefix)) {
                            array.add(stackEntry.getValue().meta());
                        }
                    }

                    return array;
                });
            });
        });
    }

    @Override
    public StructValue delegate() {
        return constants;
    }

    public RepoLoader loader() {
        return loader;
    }
}
