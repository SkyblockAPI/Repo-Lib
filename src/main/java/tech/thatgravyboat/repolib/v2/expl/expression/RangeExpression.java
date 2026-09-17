package tech.thatgravyboat.repolib.v2.expl.expression;

import tech.thatgravyboat.repolib.v2.binary.ByteBufferImpl;
import tech.thatgravyboat.repolib.v2.binary.DecoderContext;
import tech.thatgravyboat.repolib.v2.binary.EncoderContext;
import tech.thatgravyboat.repolib.v2.binary.ExpressionCodec;
import tech.thatgravyboat.repolib.v2.binary.ExpressionTypeRegistry;
import tech.thatgravyboat.repolib.v2.binary.ExpressionTypes;
import tech.thatgravyboat.repolib.v2.binary.NameTable;
import tech.thatgravyboat.repolib.v2.expl.Evaluator;
import tech.thatgravyboat.repolib.v2.expl.value.MutableArrayValue;
import tech.thatgravyboat.repolib.v2.expl.value.NumValue;
import tech.thatgravyboat.repolib.v2.expl.value.Value;

import java.io.IOException;
import java.util.LinkedList;

public record RangeExpression(boolean inclusiveStart, boolean inclusiveEnd, Expression from, Expression to)
        implements SelfEvaluatingExpression {
    @Override
    public Value evaluate(Evaluator evaluator) {
        var number = evaluator.getNumberOrThrow(evaluator.eval0(from));
        var second = evaluator.getNumberOrThrow(evaluator.eval0(to));

        var array = new LinkedList<Value>();
        var range = (second - number - (inclusiveEnd ? 0 : 1));
        if (range < 0) {
            return evaluator.panic("Num range start smaller then end!");
        }
        for (int i = inclusiveStart ? 0 : 1; i <= range; i++) {
            array.add(new NumValue(number + i));
        }
        return MutableArrayValue.create(array);
    }

    @Override
    public ExpressionTypeRegistry.Type<?> expressionId() {
        return ExpressionTypes.RANGE;
    }

    @Override
    public void precode(NameTable table) {
        table.insert(this.from);
        table.insert(this.to);
    }

    @Override
    public void encode(EncoderContext buffer) {
        byte set = (byte) (inclusiveStart ? 1 : 0);
        if (inclusiveEnd) {
            set |= 2;
        }
        buffer.writeByte(set);
        ExpressionCodec.write(this.from, buffer);
        ExpressionCodec.write(this.to, buffer);
    }

    public static RangeExpression decode(DecoderContext buffer) throws IOException {
        byte set = buffer.readByte();

        return new RangeExpression(
                (set & 1) == 1,
                (set & 2) == 2,
                ExpressionCodec.read(buffer),
                ExpressionCodec.read(buffer)
        );
    }
}
