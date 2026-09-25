package tech.thatgravyboat.repolib.v2.expl.expression;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import tech.thatgravyboat.repolib.v2.binary.BinaryCodec;
import tech.thatgravyboat.repolib.v2.binary.BinaryRecordBuilder;
import tech.thatgravyboat.repolib.v2.binary.ExpressionTypeRegistry;
import tech.thatgravyboat.repolib.v2.binary.ExpressionTypes;

public record ForExpression(
    @Nullable Expression<?> init, @Nullable Expression<?> cond, @Nullable Expression<?> incr, Expression<?> body
) implements Expression<ForExpression> {

    public static final BinaryCodec<ForExpression> CODEC = BinaryRecordBuilder.of(
        BinaryCodec.EXPRESSION.nullable().forGetter(ForExpression::init),
        BinaryCodec.EXPRESSION.nullable().forGetter(ForExpression::cond),
        BinaryCodec.EXPRESSION.nullable().forGetter(ForExpression::incr),
        BinaryCodec.EXPRESSION.forGetter(ForExpression::body),
        ForExpression::new);

    @Override
    public @NotNull String toString() {
        return "for (%s;%s;%s) %s".formatted(
            init == null ? "" : init,
            cond == null ? "" : cond,
            incr == null ? "" : incr,
            body);
    }

    @Override
    public boolean requiresSemicolon() {
        return false;
    }


    @Override
    public ExpressionTypeRegistry.Type<ForExpression> expressionId() {
        return ExpressionTypes.FOR;
    }

}
