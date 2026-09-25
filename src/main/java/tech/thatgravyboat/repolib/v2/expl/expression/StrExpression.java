package tech.thatgravyboat.repolib.v2.expl.expression;

import org.jetbrains.annotations.NotNull;
import tech.thatgravyboat.repolib.v2.binary.BinaryCodec;
import tech.thatgravyboat.repolib.v2.binary.ExpressionTypeRegistry;
import tech.thatgravyboat.repolib.v2.binary.ExpressionTypes;

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

}
