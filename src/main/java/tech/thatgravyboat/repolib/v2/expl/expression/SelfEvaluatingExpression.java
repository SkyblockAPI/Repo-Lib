package tech.thatgravyboat.repolib.v2.expl.expression;

import tech.thatgravyboat.repolib.v2.expl.Evaluator;
import tech.thatgravyboat.repolib.v2.expl.value.Value;

non-sealed public interface SelfEvaluatingExpression extends Expression {
    Value evaluate(Evaluator evaluator);
}
