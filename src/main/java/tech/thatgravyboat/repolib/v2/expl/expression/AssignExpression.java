package tech.thatgravyboat.repolib.v2.expl.expression;

import org.jetbrains.annotations.NotNull;
import tech.thatgravyboat.repolib.v2.binary.BinaryCodec;
import tech.thatgravyboat.repolib.v2.binary.BinaryRecordBuilder;
import tech.thatgravyboat.repolib.v2.binary.ExpressionTypeRegistry;
import tech.thatgravyboat.repolib.v2.binary.ExpressionTypes;

public record AssignExpression(AccessExpression lhs, Expression<?> value) implements Expression<AssignExpression> {

    public static final BinaryCodec<AssignExpression> CODEC = BinaryRecordBuilder.of(
        AccessExpression.CODEC.forGetter(AssignExpression::lhs),
        BinaryCodec.EXPRESSION.forGetter(AssignExpression::value),
        AssignExpression::new);

    @Override
    public @NotNull String toString() {
        return lhs + " = " + value;
    }

    @Override
    public boolean requiresSemicolon() {
        return value.requiresSemicolon();
    }

    @Override
    public ExpressionTypeRegistry.Type<AssignExpression> expressionId() {
        return ExpressionTypes.ASSIGN;
    }
}
