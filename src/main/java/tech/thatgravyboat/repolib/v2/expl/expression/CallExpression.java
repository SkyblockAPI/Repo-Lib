package tech.thatgravyboat.repolib.v2.expl.expression;

import java.util.Collection;
import java.util.stream.Collectors;
import org.jetbrains.annotations.NotNull;
import tech.thatgravyboat.repolib.v2.binary.BinaryCodec;
import tech.thatgravyboat.repolib.v2.binary.BinaryRecordBuilder;
import tech.thatgravyboat.repolib.v2.binary.ExpressionTypeRegistry;
import tech.thatgravyboat.repolib.v2.binary.ExpressionTypes;

public record CallExpression(Expression<?> lhs, Collection<Expression<?>> args) implements Expression<CallExpression> {

    public static final BinaryCodec<CallExpression> CODEC = BinaryRecordBuilder.of(
        BinaryCodec.EXPRESSION.forGetter(CallExpression::lhs),
        BinaryCodec.EXPRESSION.collection().forGetter(CallExpression::args),
        CallExpression::new);

    @Override
    public @NotNull String toString() {
        return lhs + "(" + args.stream().map(Expression::toString).collect(Collectors.joining(", ")) + ")";
    }

    @Override
    public ExpressionTypeRegistry.Type<CallExpression> expressionId() {
        return ExpressionTypes.CALL;
    }


}
