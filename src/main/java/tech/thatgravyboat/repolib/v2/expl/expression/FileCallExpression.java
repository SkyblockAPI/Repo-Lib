package tech.thatgravyboat.repolib.v2.expl.expression;


import org.jetbrains.annotations.NotNull;
import tech.thatgravyboat.repolib.v2.binary.BinaryCodec;
import tech.thatgravyboat.repolib.v2.binary.BinaryRecordBuilder;
import tech.thatgravyboat.repolib.v2.binary.ExpressionTypeRegistry;
import tech.thatgravyboat.repolib.v2.binary.ExpressionTypes;
import tech.thatgravyboat.repolib.v2.expl.Evaluator;
import tech.thatgravyboat.repolib.v2.expl.value.Value;

public record FileCallExpression(Expression<?> access, StructExpression expr)
    implements Expression<FileCallExpression> {

    public static final BinaryCodec<FileCallExpression> CODEC = BinaryRecordBuilder.of(
        BinaryCodec.EXPRESSION.forGetter(FileCallExpression::access),
        StructExpression.CODEC.forGetter(FileCallExpression::expr),
        FileCallExpression::new);

    @Override
    public @NotNull String toString() {
        return access + " " + expr;
    }

    @Override
    public Value evaluate(Evaluator evaluator) {
        var function = evaluator.eval0(access).asStructuredFunction();
        var structValue = expr.evaluate(evaluator);

        return function.apply(evaluator, structValue);
    }

    @Override
    public ExpressionTypeRegistry.Type<FileCallExpression> expressionId() {
        return ExpressionTypes.FILE_CALL;
    }


}

