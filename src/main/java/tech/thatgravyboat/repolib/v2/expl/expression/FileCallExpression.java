package tech.thatgravyboat.repolib.v2.expl.expression;


import org.jetbrains.annotations.NotNull;
import tech.thatgravyboat.repolib.v2.binary.ByteBuffer;
import tech.thatgravyboat.repolib.v2.binary.ExpressionCodec;
import tech.thatgravyboat.repolib.v2.binary.ExpressionTypeRegistry;
import tech.thatgravyboat.repolib.v2.binary.ExpressionTypes;
import tech.thatgravyboat.repolib.v2.expl.Evaluator;
import tech.thatgravyboat.repolib.v2.expl.value.Value;

import java.io.IOException;

public record FileCallExpression(Expression access, StructExpression expr) implements SelfEvaluatingExpression {

    @Override
    public @NotNull String toString() {
        return access + " " + expr;
    }

    @Override
    public Value evaluate(Evaluator evaluator) {
        var function = evaluator.getStructuredFunctionOrThrow(evaluator.eval0(access));
        var structValue = evaluator.evalStruct(expr);

        return function.apply(evaluator, structValue);
    }

    @Override
    public ExpressionTypeRegistry.Type<?> expressionId() {
        return ExpressionTypes.FILE_CALL;
    }

    @Override
    public void encode(ByteBuffer buffer) {
        ExpressionCodec.write(this.access, buffer);
        ExpressionCodec.writeUntyped(this.expr, buffer);
    }

    public static FileCallExpression decode(ByteBuffer buffer) throws IOException {
        return new FileCallExpression(
                ExpressionCodec.read(buffer),
                ExpressionCodec.readUntyped(ExpressionTypes.STRUCT, buffer)
        );
    }
}

