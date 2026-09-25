package tech.thatgravyboat.repolib.v2.expl.expression;

import tech.thatgravyboat.repolib.v2.binary.BinaryCodec;
import tech.thatgravyboat.repolib.v2.binary.ExpressionTypeRegistry;
import tech.thatgravyboat.repolib.v2.binary.ExpressionTypes;
import tech.thatgravyboat.repolib.v2.expl.Evaluator;
import tech.thatgravyboat.repolib.v2.expl.value.Value;

public record LambdaIdentityFunction(LambdaExpression expression)
    implements SelfEvaluatingExpression<LambdaIdentityFunction> {

    public static final BinaryCodec<LambdaIdentityFunction> CODEC =
        LambdaExpression.CODEC.mapped(LambdaIdentityFunction::new, LambdaIdentityFunction::expression);

    @Override
    public Value evaluate(Evaluator evaluator) {
        return null;
    }

    @Override
    public ExpressionTypeRegistry.Type<LambdaIdentityFunction> expressionId() {
        return ExpressionTypes.LAMBDA_IDENTITY;
    }

}
