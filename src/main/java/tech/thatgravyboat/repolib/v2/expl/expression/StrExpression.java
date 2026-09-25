package tech.thatgravyboat.repolib.v2.expl.expression;

import org.jetbrains.annotations.NotNull;
import tech.thatgravyboat.repolib.v2.binary.BinaryCodec;
import tech.thatgravyboat.repolib.v2.binary.ExpressionTypeRegistry;
import tech.thatgravyboat.repolib.v2.binary.ExpressionTypes;
import tech.thatgravyboat.repolib.v2.expl.Evaluator;
import tech.thatgravyboat.repolib.v2.expl.value.StrValue;
import tech.thatgravyboat.repolib.v2.expl.value.Value;

public record StrExpression(String value) implements Expression<StrExpression> {

    public static final BinaryCodec<StrExpression> CODEC =
        BinaryCodec.STRING.mapped(StrExpression::new, StrExpression::value);

    @Override
    public @NotNull String toString() {
        return "\"" + value + "\"";
    }

    @Override
    public ExpressionTypeRegistry.Type<StrExpression> expressionId() {
        return ExpressionTypes.STRING;
    }

    @Override
    public Value evaluate(Evaluator evaluator) {
        return new StrValue(this.value);
    }
}
