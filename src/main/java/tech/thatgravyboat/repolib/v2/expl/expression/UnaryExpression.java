package tech.thatgravyboat.repolib.v2.expl.expression;

import org.jetbrains.annotations.NotNull;
import tech.thatgravyboat.repolib.v2.binary.BinaryCodec;
import tech.thatgravyboat.repolib.v2.binary.BinaryRecordBuilder;
import tech.thatgravyboat.repolib.v2.binary.ExpressionTypeRegistry;
import tech.thatgravyboat.repolib.v2.binary.ExpressionTypes;
import tech.thatgravyboat.repolib.v2.expl.Evaluator;
import tech.thatgravyboat.repolib.v2.expl.value.BoolValue;
import tech.thatgravyboat.repolib.v2.expl.value.NumValue;
import tech.thatgravyboat.repolib.v2.expl.value.Value;

public record UnaryExpression(Op op, Expression<?> rhs) implements Expression<UnaryExpression> {

    public static BinaryCodec<UnaryExpression> CODEC = BinaryRecordBuilder.of(
        Op.CODEC.forGetter(UnaryExpression::op),
        BinaryCodec.EXPRESSION.forGetter(UnaryExpression::rhs),
        UnaryExpression::new);

    @Override
    public @NotNull String toString() {
        return switch (op) {
            case NEGATE -> "-" + rhs;
            case NOT -> "!" + rhs;
        };
    }

    @Override
    public ExpressionTypeRegistry.Type<UnaryExpression> expressionId() {
        return ExpressionTypes.UNARY;
    }

    @Override
    public Value evaluate(Evaluator evaluator) {
        return op().evaluate(evaluator.eval0(rhs()));

    }

    public enum Op {
        NEGATE {
            @Override
            public Value evaluate(Value value) {
                return new NumValue(-value.asNumber());
            }
        },
        NOT {
            @Override
            public Value evaluate(Value value) {
                return BoolValue.wrap(!value.asBooleanConversion());
            }
        },
        ;

        public abstract Value evaluate(Value value);
        public static final BinaryCodec<Op> CODEC = BinaryCodec.enumCodec(values());
    }
}

