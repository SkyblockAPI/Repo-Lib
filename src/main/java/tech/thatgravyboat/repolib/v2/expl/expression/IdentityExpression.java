package tech.thatgravyboat.repolib.v2.expl.expression;

import java.util.function.Function;
import tech.thatgravyboat.repolib.v2.expl.Evaluator;
import tech.thatgravyboat.repolib.v2.expl.value.Value;

public record IdentityExpression(Function<Value, Value> valueFunction) implements SelfEvaluatingExpression {
    @Override
    public Value evaluate(Evaluator evaluator) {
        return null;
    }
}
