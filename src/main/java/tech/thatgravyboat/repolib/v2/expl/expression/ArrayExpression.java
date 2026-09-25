package tech.thatgravyboat.repolib.v2.expl.expression;

import java.util.ArrayList;
import java.util.Collection;
import org.jetbrains.annotations.NotNull;
import tech.thatgravyboat.repolib.v2.binary.BinaryCodec;
import tech.thatgravyboat.repolib.v2.binary.ExpressionTypeRegistry;
import tech.thatgravyboat.repolib.v2.binary.ExpressionTypes;
import tech.thatgravyboat.repolib.v2.expl.Evaluator;
import tech.thatgravyboat.repolib.v2.expl.value.MutableArrayValue;
import tech.thatgravyboat.repolib.v2.expl.value.Value;

public record ArrayExpression(Collection<Expression<?>> list) implements Expression<ArrayExpression> {

    public static final BinaryCodec<ArrayExpression> CODEC =
        BinaryCodec.EXPRESSION.collection().mapped(ArrayExpression::new, ArrayExpression::list);

    @Override
    public @NotNull String toString() {
        return list.toString();
    }


    @Override
    public Value evaluate(Evaluator evaluator) {
        var array = MutableArrayValue.create(new ArrayList<>());

        for (var expression : list) {
            array.add(evaluator.eval0(expression));
        }

        return array;
    }

    @Override
    public ExpressionTypeRegistry.Type<ArrayExpression> expressionId() {
        return ExpressionTypes.ARRAY;
    }


}
