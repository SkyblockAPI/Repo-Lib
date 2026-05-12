package tech.thatgravyboat.repolib.v2.expl.value;

import tech.thatgravyboat.repolib.v2.builtin.Constants;
import tech.thatgravyboat.repolib.v2.expl.Evaluator;

import java.util.List;
import java.util.function.Consumer;

@FunctionalInterface
non-sealed public interface FunctionValue extends Value {

    public static FunctionValue builder(Consumer<Constants.Builder.FunctionBuilder> builder) {
        return Constants.Builder.FunctionBuilder.create(builder);
    }

    Value apply(Evaluator evaluator, List<Value> args);
}
