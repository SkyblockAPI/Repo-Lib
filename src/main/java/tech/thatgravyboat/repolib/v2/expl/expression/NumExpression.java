package tech.thatgravyboat.repolib.v2.expl.expression;

import org.jetbrains.annotations.NotNull;
import tech.thatgravyboat.repolib.v2.binary.BinaryCodec;
import tech.thatgravyboat.repolib.v2.binary.ExpressionTypeRegistry;
import tech.thatgravyboat.repolib.v2.binary.ExpressionTypes;
import tech.thatgravyboat.repolib.v2.expl.Evaluator;
import tech.thatgravyboat.repolib.v2.expl.value.NumValue;
import tech.thatgravyboat.repolib.v2.expl.value.Value;

public record NumExpression(double value) implements Expression<NumExpression> {

    public static final BinaryCodec<NumExpression> CODEC =
        BinaryCodec.DOUBLE.mapped(NumExpression::new, NumExpression::value);

    @Override
    public @NotNull String toString() {
        return String.valueOf(value);
    }

    @Override
    public ExpressionTypeRegistry.Type<NumExpression> expressionId() {
        return ExpressionTypes.NUMBER;
    }

    @Override
    public Value evaluate(Evaluator evaluator) {
        return new NumValue(this.value);
    }
}
