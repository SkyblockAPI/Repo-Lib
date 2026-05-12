package tech.thatgravyboat.repolib.v2.expl.expression;

import org.jetbrains.annotations.NotNull;
import tech.thatgravyboat.repolib.v2.expl.Evaluator;
import tech.thatgravyboat.repolib.v2.expl.value.MutableArray;
import tech.thatgravyboat.repolib.v2.expl.value.Value;

import java.util.ArrayList;
import java.util.List;

public record Array(List<Expression> list) implements SelfEvaluatingExpression {
    @Override
    public @NotNull String toString() {
        return list.toString();
    }

    @Override
    public Value evaluate(Evaluator evaluator) {
        var array = MutableArray.create(new ArrayList<>());

        for (var expression : list) {
            array.add(evaluator.eval0(expression));
        }

        return array;
    }
}
