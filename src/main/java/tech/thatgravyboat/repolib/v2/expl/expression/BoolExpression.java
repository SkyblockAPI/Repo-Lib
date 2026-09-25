package tech.thatgravyboat.repolib.v2.expl.expression;

import org.jetbrains.annotations.NotNull;
import tech.thatgravyboat.repolib.v2.binary.BinaryCodec;
import tech.thatgravyboat.repolib.v2.binary.ExpressionTypeRegistry;
import tech.thatgravyboat.repolib.v2.binary.ExpressionTypes;

public record BoolExpression(boolean value) implements Expression<BoolExpression> {

    public static final BinaryCodec<BoolExpression> CODEC =
        BinaryCodec.BOOLEAN.mapped(BoolExpression::new, BoolExpression::value);

    @Override
    public @NotNull String toString() {
        return Boolean.toString(value);
    }

    @Override
    public ExpressionTypeRegistry.Type<BoolExpression> expressionId() {
        return ExpressionTypes.BOOLEAN;
    }

}
