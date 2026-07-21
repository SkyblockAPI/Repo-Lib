package tech.thatgravyboat.repolib.v2.expl.expression;

import java.util.List;
import tech.thatgravyboat.repolib.v2.expl.Evaluator;
import tech.thatgravyboat.repolib.v2.expl.value.FunctionValue;
import tech.thatgravyboat.repolib.v2.expl.value.Value;

public record LambdaExpression(List<LambdaArgument> arguments, Expression body) implements SelfEvaluatingExpression {
    @Override
    public Value evaluate(Evaluator _evaluator) {
        return FunctionValue.builder(builder -> {
            builder.arity(arguments.size());
            builder.execute((evaluator, values) -> evaluator.pushPop("lambda", () -> {
                for (var argument : arguments) {
                    evaluator.set(argument.name, values.get(argument.position));
                }

                return evaluator.evaluate(body);
            }));
        });
    }

    @Override
    public boolean requiresSemicolon() {
        return body.requiresSemicolon();
    }

    @Override
    public boolean canReturnValueBeReturned() {
        return true;
    }

    public record LambdaArgument(String name, int position) {}
}
