package tech.thatgravyboat.repolib.v2.expl.expression;

import tech.thatgravyboat.repolib.v2.expl.Evaluator;
import tech.thatgravyboat.repolib.v2.expl.value.Value;

non-sealed public interface SelfEvaluatingExpression<ExpressionType extends Expression<ExpressionType>>
    extends Expression<ExpressionType> {
    Value evaluate(Evaluator evaluator);
}
