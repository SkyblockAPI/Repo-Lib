package tech.thatgravyboat.repolib.v2.expl;

import java.util.List;
import tech.thatgravyboat.repolib.v2.RepoLoader;
import tech.thatgravyboat.repolib.v2.expl.expression.Expression;
import tech.thatgravyboat.repolib.v2.expl.expression.LambdaExpression;
import tech.thatgravyboat.repolib.v2.expl.value.LambdaValue;
import tech.thatgravyboat.repolib.v2.expl.value.StructValue;
import tech.thatgravyboat.repolib.v2.expl.value.StructuredFunctionValue;
import tech.thatgravyboat.repolib.v2.expl.value.Value;

public record FunctionFile(RepoLoader loader, String name, List<LambdaExpression.LambdaArgument> arguments, Expression body) implements LambdaValue, StructuredFunctionValue {

    @Override
    public Value apply(Evaluator evaluator, StructValue structValue) {
        for (var argument : arguments) {
            if (argument.optional()) continue;
            if (!structValue.contains(argument.name())) {
                return evaluator.panic("Missing required argument on " + name);
            }
        }

        return evaluator.pushPop(name, structValue.toMutableStruct(), () -> evaluator.evaluate(this.body));
    }

    @Override
    public Value apply(Evaluator evaluator, List<Value> values) {

        return evaluator.pushPop(name, () -> {

            for (var argument : arguments) {
                if (argument.optional() && argument.position() >= values.size()) continue;
                evaluator.set(argument.name(), values.get(argument.position()));
            }

            return evaluator.evaluate(body);
        });
    }

    @Override
    public boolean vararg() {
        int min = 0;
        int max = 0;
        boolean hasEncounteredOptional = false;

        for (var argument : arguments) {
            max++;

            if (argument.optional()) {
                hasEncounteredOptional = true;
                continue;
            }

            if (hasEncounteredOptional) {
                throw new IllegalStateException("Optional before required argument!");
            }

            min++;
        }

        return min != max;
    }

    @Override
    public int arityMin() {
        int min = 0;
        for (var argument : arguments) {
            if (argument.optional()) continue;

            min++;
        }

        return min;
    }

    @Override
    public int arityMax() {
        return this.arguments.size();
    }
}
