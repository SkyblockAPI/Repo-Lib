package tech.thatgravyboat.repolib.v2.expl.expression;

import org.jetbrains.annotations.NotNull;
import tech.thatgravyboat.repolib.v2.binary.ByteBufferImpl;
import tech.thatgravyboat.repolib.v2.binary.DecoderContext;
import tech.thatgravyboat.repolib.v2.binary.EncoderContext;
import tech.thatgravyboat.repolib.v2.binary.ExpressionCodec;
import tech.thatgravyboat.repolib.v2.binary.ExpressionTypeRegistry;
import tech.thatgravyboat.repolib.v2.binary.ExpressionTypes;
import tech.thatgravyboat.repolib.v2.binary.NameTable;
import tech.thatgravyboat.repolib.v2.expl.Evaluator;
import tech.thatgravyboat.repolib.v2.expl.value.MutableArrayValue;
import tech.thatgravyboat.repolib.v2.expl.value.Value;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;

public record ArrayExpression(Collection<Expression> list) implements SelfEvaluatingExpression {
    @Override
    public @NotNull String toString() {
        return list.toString();
    }

    @Override
    public Value evaluate(Evaluator evaluator) {
        var array = MutableArrayValue.create(new ArrayList<>());

        for (var expression : list) {
            array.add(evaluator.eval0(expression));
        }

        return array;
    }

    @Override
    public ExpressionTypeRegistry.Type<?> expressionId() {
        return ExpressionTypes.ARRAY;
    }

    @Override
    public void precode(NameTable table) {
        for (var expression : this.list) {
            expression.precode(table);
        }
    }

    @Override
    public void encode(EncoderContext buffer) {
        buffer.writeCollection(this.list, ExpressionCodec::write);
    }

    public static ArrayExpression decode(DecoderContext buffer) throws IOException {
        return new ArrayExpression(buffer.readCollection(ExpressionCodec::read));
    }
}
