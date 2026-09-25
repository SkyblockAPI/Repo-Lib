package tech.thatgravyboat.repolib.v2.expl.expression;

import tech.thatgravyboat.repolib.v2.binary.BinaryCodec;
import tech.thatgravyboat.repolib.v2.binary.BinaryRecordBuilder;
import tech.thatgravyboat.repolib.v2.binary.ExpressionTypeRegistry;
import tech.thatgravyboat.repolib.v2.binary.ExpressionTypes;
import tech.thatgravyboat.repolib.v2.expl.Evaluator;
import tech.thatgravyboat.repolib.v2.expl.value.BoolValue;
import tech.thatgravyboat.repolib.v2.expl.value.KeyValue;
import tech.thatgravyboat.repolib.v2.expl.value.StrValue;
import tech.thatgravyboat.repolib.v2.expl.value.Value;

public record InExpression(AccessExpression holder, Expression<?> field) implements Expression<InExpression> {

    public static final BinaryCodec<InExpression> CODEC = BinaryRecordBuilder.of(
        AccessExpression.CODEC.forGetter(InExpression::holder),
        BinaryCodec.EXPRESSION.forGetter(InExpression::field),
        InExpression::new);

    @Override
    public ExpressionTypeRegistry.Type<InExpression> expressionId() {
        return ExpressionTypes.IN;
    }

    @Override
    public Value evaluate(Evaluator evaluator) {
        var holder = holder().evaluate(evaluator);
        String field = evaluator.eval0(field()).asString();

        return holder.containsValue(field);
    }
}
