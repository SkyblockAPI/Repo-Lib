package tech.thatgravyboat.repolib.v2.expl.expression;

import tech.thatgravyboat.repolib.v2.binary.BinaryCodec;
import tech.thatgravyboat.repolib.v2.binary.ExpressionTypeRegistry;
import tech.thatgravyboat.repolib.v2.binary.ExpressionTypes;
import tech.thatgravyboat.repolib.v2.expl.Evaluator;
import tech.thatgravyboat.repolib.v2.expl.ExecutionExceptions;
import tech.thatgravyboat.repolib.v2.expl.value.Value;

public record ReturnExpression(Expression<?> retExpr) implements Expression<ReturnExpression> {

    public static final BinaryCodec<ReturnExpression> CODEC =
        BinaryCodec.EXPRESSION.mapped(ReturnExpression::new, ReturnExpression::retExpr);

    @Override
    public Value evaluate(Evaluator evaluator) {
        throw new ExecutionExceptions.Return(evaluator.eval0(retExpr));
    }

    @Override
    public ExpressionTypeRegistry.Type<ReturnExpression> expressionId() {
        return ExpressionTypes.RETURN;
    }

}
