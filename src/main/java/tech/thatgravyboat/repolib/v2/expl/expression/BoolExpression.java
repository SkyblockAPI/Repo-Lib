package tech.thatgravyboat.repolib.v2.expl.expression;

import org.jetbrains.annotations.NotNull;
import tech.thatgravyboat.repolib.v2.binary.BinaryCodec;
import tech.thatgravyboat.repolib.v2.binary.ExpressionTypeRegistry;
import tech.thatgravyboat.repolib.v2.binary.ExpressionTypes;
import tech.thatgravyboat.repolib.v2.expl.Evaluator;
import tech.thatgravyboat.repolib.v2.expl.value.BoolValue;
import tech.thatgravyboat.repolib.v2.expl.value.Value;

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

    @Override
    public Value evaluate(Evaluator evaluator) {
        return BoolValue.wrap(this.value);
    }
}
