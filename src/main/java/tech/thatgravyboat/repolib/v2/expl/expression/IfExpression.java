package tech.thatgravyboat.repolib.v2.expl.expression;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import tech.thatgravyboat.repolib.v2.binary.BinaryCodec;
import tech.thatgravyboat.repolib.v2.binary.BinaryRecordBuilder;
import tech.thatgravyboat.repolib.v2.binary.ExpressionTypeRegistry;
import tech.thatgravyboat.repolib.v2.binary.ExpressionTypes;

public record IfExpression(Expression<?> cond, Expression<?> thenExpr, @Nullable Expression<?> elseExpr)
    implements Expression<IfExpression> {

    public static final BinaryCodec<IfExpression> CODEC = BinaryRecordBuilder.of(
        BinaryCodec.EXPRESSION.forGetter(IfExpression::cond),
        BinaryCodec.EXPRESSION.forGetter(IfExpression::thenExpr),
        BinaryCodec.EXPRESSION.nullable().forGetter(IfExpression::elseExpr),
        IfExpression::new);

    @Override
    public @NotNull String toString() {
        if (elseExpr == null) {
            return String.format("if (%s) %s", cond, thenExpr);
        }
        return String.format("if (%s) %s else %s", cond, thenExpr, elseExpr);
    }

    @Override
    public boolean requiresSemicolon() {
        return false;
    }

    @Override
    public ExpressionTypeRegistry.Type<IfExpression> expressionId() {
        return ExpressionTypes.IF;
    }
}
