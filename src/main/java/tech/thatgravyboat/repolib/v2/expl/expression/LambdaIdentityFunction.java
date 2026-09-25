package tech.thatgravyboat.repolib.v2.expl.expression;

import java.io.IOException;
import tech.thatgravyboat.repolib.v2.binary.DecoderContext;
import tech.thatgravyboat.repolib.v2.binary.EncoderContext;
import tech.thatgravyboat.repolib.v2.binary.ExpressionCodec;
import tech.thatgravyboat.repolib.v2.binary.ExpressionTypeRegistry;
import tech.thatgravyboat.repolib.v2.binary.ExpressionTypes;
import tech.thatgravyboat.repolib.v2.binary.NameTable;
import tech.thatgravyboat.repolib.v2.expl.Evaluator;
import tech.thatgravyboat.repolib.v2.expl.value.Value;

public record LambdaIdentityFunction(LambdaExpression expression) implements SelfEvaluatingExpression {
    @Override
    public Value evaluate(Evaluator evaluator) {
        return null;
    }

    @Override
    public ExpressionTypeRegistry.Type<?> expressionId() {
        return ExpressionTypes.LAMBDA_IDENTITY;
    }

    @Override
    public void precode(NameTable table) {
        table.insert(this.expression);
    }

    @Override
    public void encode(EncoderContext buffer) {
        ExpressionCodec.writeUntyped(this.expression, buffer);
    }

    public static LambdaIdentityFunction decode(DecoderContext buffer) throws IOException {
        return new LambdaIdentityFunction(ExpressionCodec.readUntyped(ExpressionTypes.LAMBDA, buffer));
    }

}
