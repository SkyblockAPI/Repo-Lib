package tech.thatgravyboat.repolib.v2.expl.expression;

import java.util.LinkedList;
import tech.thatgravyboat.repolib.v2.binary.BinaryCodec;
import tech.thatgravyboat.repolib.v2.binary.BinaryRecordBuilder;
import tech.thatgravyboat.repolib.v2.binary.ExpressionTypeRegistry;
import tech.thatgravyboat.repolib.v2.binary.ExpressionTypes;
import tech.thatgravyboat.repolib.v2.expl.Evaluator;
import tech.thatgravyboat.repolib.v2.expl.value.MutableArrayValue;
import tech.thatgravyboat.repolib.v2.expl.value.NumValue;
import tech.thatgravyboat.repolib.v2.expl.value.Value;

public record RangeExpression(boolean inclusiveStart, boolean inclusiveEnd, Expression<?> from, Expression<?> to)
    implements Expression<RangeExpression> {

    public static final BinaryCodec<RangeExpression> CODEC = BinaryRecordBuilder.of(
        BinaryCodec.BYTE.forGetter(owner -> {
            byte set = (byte) (owner.inclusiveStart ? 1 : 0);
            if (owner.inclusiveEnd) {
                set |= 2;
            }
            return set;
        }),
        BinaryCodec.EXPRESSION.forGetter(RangeExpression::from),
        BinaryCodec.EXPRESSION.forGetter(RangeExpression::to),
        (packed, from, to) -> new RangeExpression((packed & 1) == 1, (packed & 2) == 2, from, to));

    @Override
    public Value evaluate(Evaluator evaluator) {
        var number = evaluator.eval0(from).asNumber();
        var second = evaluator.eval0(to).asNumber();

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
    public ExpressionTypeRegistry.Type<RangeExpression> expressionId() {
        return ExpressionTypes.RANGE;
    }
}
