package tech.thatgravyboat.repolib.v2.expl.expression;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import tech.thatgravyboat.repolib.v2.binary.BinaryCodec;
import tech.thatgravyboat.repolib.v2.binary.BinaryRecordBuilder;
import tech.thatgravyboat.repolib.v2.binary.ExpressionTypeRegistry;
import tech.thatgravyboat.repolib.v2.binary.ExpressionTypes;

public record AccessExpression(@Nullable Expression<?> lhs, Expression<?> field)
    implements Expression<AccessExpression> {

    public static BinaryCodec<AccessExpression> CODEC = BinaryRecordBuilder.of(
        BinaryCodec.NULLABLE_EXPRESSION.forGetter(AccessExpression::lhs),
        BinaryCodec.EXPRESSION.forGetter(AccessExpression::field),
        AccessExpression::new);

    @Override
    public @NotNull String toString() {
        return lhs + "." + field;
    }

    @Override
    public ExpressionTypeRegistry.Type<AccessExpression> expressionId() {
        return ExpressionTypes.ACCESS;
    }
}
