package tech.thatgravyboat.repolib.v2.expl.expression;

import org.jetbrains.annotations.NotNull;
import tech.thatgravyboat.repolib.v2.binary.BinaryCodec;
import tech.thatgravyboat.repolib.v2.binary.BinaryRecordBuilder;
import tech.thatgravyboat.repolib.v2.binary.ExpressionTypeRegistry;
import tech.thatgravyboat.repolib.v2.binary.ExpressionTypes;

public record UnaryExpression(Op op, Expression<?> rhs) implements Expression<UnaryExpression> {

    public static BinaryCodec<UnaryExpression> CODEC = BinaryRecordBuilder.of(
        Op.CODEC.forGetter(UnaryExpression::op),
        BinaryCodec.EXPRESSION.forGetter(UnaryExpression::rhs),
        UnaryExpression::new);

    @Override
    public @NotNull String toString() {
        return switch (op) {
            case NEGATE -> "-" + rhs;
            case NOT -> "!" + rhs;
        };
    }

    @Override
    public ExpressionTypeRegistry.Type<UnaryExpression> expressionId() {
        return ExpressionTypes.UNARY;
    }

    public enum Op {
        NEGATE,
        NOT,
        ;
        public static final BinaryCodec<Op> CODEC = BinaryCodec.enumCodec(values());
    }
}

