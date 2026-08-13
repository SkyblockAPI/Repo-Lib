package tech.thatgravyboat.repolib.v2.expl.expression;


import org.jetbrains.annotations.NotNull;
import tech.thatgravyboat.repolib.v2.expl.Evaluator;
import tech.thatgravyboat.repolib.v2.expl.value.Value;

public record FileCallExpression(Expression access, StructExpression expr) implements SelfEvaluatingExpression {

    @Override
    public @NotNull String toString() {
        return access + " " + expr;
    }

    @Override
    public Value evaluate(Evaluator evaluator) {
        var function = evaluator.getStructuredFunctionOrThrow(evaluator.eval0(access));
        var structValue = evaluator.evalStruct(expr);

        return function.apply(evaluator, structValue);
    }
}

