package tech.thatgravyboat.repolib.v2.expl.expression;

import java.util.ArrayList;
import java.util.Collection;
import java.util.stream.Collectors;
import org.jetbrains.annotations.NotNull;
import tech.thatgravyboat.repolib.v2.binary.BinaryCodec;
import tech.thatgravyboat.repolib.v2.binary.BinaryRecordBuilder;
import tech.thatgravyboat.repolib.v2.binary.ExpressionTypeRegistry;
import tech.thatgravyboat.repolib.v2.binary.ExpressionTypes;
import tech.thatgravyboat.repolib.v2.expl.Evaluator;
import tech.thatgravyboat.repolib.v2.expl.value.Value;

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

    @Override
    public Value evaluate(Evaluator evaluator) {
        var left = evaluator.eval0(lhs());

        var args = new ArrayList<Value>();
        for (var arg : args()) {
            args.add(evaluator.eval0(arg));
        }

        return evaluator.pushPop(lhs().toString(), () -> left.asFunctionValue().apply(evaluator, args));
    }


}
