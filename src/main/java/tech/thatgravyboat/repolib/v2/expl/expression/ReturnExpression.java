package tech.thatgravyboat.repolib.v2.expl.expression;

import tech.thatgravyboat.repolib.v2.expl.Evaluator;
import tech.thatgravyboat.repolib.v2.expl.ExecutionExceptions;
import tech.thatgravyboat.repolib.v2.expl.value.Value;

public record ReturnExpression(Expression retExpr) implements SelfEvaluatingExpression {
    @Override
    public Value evaluate(Evaluator evaluator) {
        throw new ExecutionExceptions.Return(evaluator.eval0(retExpr));
    }
}
