package tech.thatgravyboat.repolib.v2.expl.expression;

import java.util.List;
import org.jetbrains.annotations.Nullable;
import tech.thatgravyboat.repolib.v2.expl.Evaluator;
import tech.thatgravyboat.repolib.v2.expl.value.FunctionValue;
import tech.thatgravyboat.repolib.v2.expl.value.Value;

public record LambdaExpression(
    List<LambdaArgument> arguments, Expression body, Value function, boolean requiresSemicolon
) implements SelfEvaluatingExpression {


    public LambdaExpression(List<LambdaArgument> arguments, Expression body) {
        this(arguments, body, null);
    }

    public LambdaExpression(List<LambdaArgument> arguments, Expression body, Value self) {
        this(
            arguments, body, FunctionValue.builder(builder -> {
                builder.arity(arguments.size());
                builder.execute((evaluator, values) -> evaluator.pushPop(
                    "lambda", () -> {
                        if (self != null) {
                            evaluator.set("self", self);
                        }

                        for (var argument : arguments) {
                            evaluator.set(argument.name, values.get(argument.position));
                        }

                        return evaluator.evaluate(body);
                    }));
            }), body.requiresSemicolon());
    }


    @Override
    public Value evaluate(Evaluator _evaluator) {
        return function;
    }

    @Override
    public boolean canReturnValueBeReturned() {
        return true;
    }

    public record LambdaArgument(String name, int position) {}
}
