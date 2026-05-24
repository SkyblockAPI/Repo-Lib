package tech.thatgravyboat.repolib.v2.expl.expression;

import tech.thatgravyboat.repolib.v2.expl.Evaluator;
import tech.thatgravyboat.repolib.v2.expl.value.MutableArrayValue;
import tech.thatgravyboat.repolib.v2.expl.value.NumValue;
import tech.thatgravyboat.repolib.v2.expl.value.Value;

import java.util.LinkedList;

public record RangeExpression(boolean inclusiveStart, boolean inclusiveEnd, Expression from, Expression to)
        implements SelfEvaluatingExpression {
    @Override
    public Value evaluate(Evaluator evaluator) {
        var number = evaluator.getNumberOrThrow(evaluator.eval0(from));
        var second = evaluator.getNumberOrThrow(evaluator.eval0(to));

        var array = new LinkedList<Value>();
        var range = (second - number - (inclusiveEnd ? 0 : 1));
        if (range < 0) {
            return evaluator.panic("Num range start smaller then end!");
        }
        for (int i = inclusiveStart ? 0 : 1; i <= range; i++) {
            array.add(new NumValue(number + i));
        }
        return MutableArrayValue.create(array);
    }
}
