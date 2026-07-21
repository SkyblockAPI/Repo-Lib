package tech.thatgravyboat.repolib.v2.expl.value;

import java.util.List;
import java.util.function.BiFunction;
import tech.thatgravyboat.repolib.v2.expl.Evaluator;

public record LambdaFunctionValue(
    BiFunction<Evaluator, List<Value>, Value> executor,
    boolean vararg,
    int arityMin,
    int arityMax
) implements FunctionValue {
    @Override
    public Value apply(Evaluator evaluator, List<Value> args) {
        return executor.apply(evaluator, args);
    }
}
