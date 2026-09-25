package tech.thatgravyboat.repolib.v2.expl.expression;

import tech.thatgravyboat.repolib.v2.binary.BinaryCodec;
import tech.thatgravyboat.repolib.v2.binary.BinaryRecordBuilder;
import tech.thatgravyboat.repolib.v2.binary.ExpressionTypeRegistry;
import tech.thatgravyboat.repolib.v2.binary.ExpressionTypes;

public record InExpression(AccessExpression holder, Expression<?> field) implements Expression<InExpression> {

    public static final BinaryCodec<InExpression> CODEC = BinaryRecordBuilder.of(
        AccessExpression.CODEC.forGetter(InExpression::holder),
        BinaryCodec.EXPRESSION.forGetter(InExpression::field),
        InExpression::new);

    @Override
    public ExpressionTypeRegistry.Type<InExpression> expressionId() {
        return ExpressionTypes.IN;
    }

}
