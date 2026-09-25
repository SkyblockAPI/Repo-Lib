package tech.thatgravyboat.repolib.v2.expl.expression;

import java.io.IOException;
import java.util.Objects;
import tech.thatgravyboat.repolib.v2.binary.BinaryCodec;
import tech.thatgravyboat.repolib.v2.binary.BinaryRecordBuilder;
import tech.thatgravyboat.repolib.v2.binary.DecoderContext;
import tech.thatgravyboat.repolib.v2.binary.ExpressionCodec;
import tech.thatgravyboat.repolib.v2.binary.ExpressionTypeRegistry;
import tech.thatgravyboat.repolib.v2.binary.ExpressionTypes;
import tech.thatgravyboat.repolib.v2.expl.Evaluator;
import tech.thatgravyboat.repolib.v2.expl.value.BoolValue;
import tech.thatgravyboat.repolib.v2.expl.value.MutableArrayValue;
import tech.thatgravyboat.repolib.v2.expl.value.NumValue;
import tech.thatgravyboat.repolib.v2.expl.value.StrValue;
import tech.thatgravyboat.repolib.v2.expl.value.Value;

public record BinaryExpression(Op op, Expression<?> first, Expression<?> second)
    implements Expression<BinaryExpression> {

    public static final BinaryCodec<BinaryExpression> CODEC = BinaryRecordBuilder.of(
        Op.CODEC.forGetter(BinaryExpression::op),
        BinaryCodec.EXPRESSION.forGetter(BinaryExpression::first),
        BinaryCodec.EXPRESSION.forGetter(BinaryExpression::second),
        BinaryExpression::new);

    @Override
    public Value evaluate(Evaluator evaluator) {
        return op.performRaw(evaluator, first, second);
    }

    @Override
    public ExpressionTypeRegistry.Type<BinaryExpression> expressionId() {
        return ExpressionTypes.BINARY;
    }

    public static BinaryExpression decode(DecoderContext buffer) throws IOException {
        return new BinaryExpression(
            Op.CODEC.decode(buffer),
            ExpressionCodec.read(buffer),
            ExpressionCodec.read(buffer));
    }

    public enum Op {
        PLUS {
            @Override
            public Value perform(Evaluator evaluator, Value first, Value second) {

                if (first instanceof NumValue(double aNum) && second instanceof NumValue(double bNum)) {
                    return new NumValue(aNum + bNum);
                } else if (first instanceof StrValue(String aStr) && second instanceof StrValue(String bStr)) {
                    return new StrValue(aStr + bStr);
                } else if (first instanceof MutableArrayValue array) {
                    array.add(second);
                    return first;
                }

                return evaluator.panic("Unable to add " + second.type() + " to " + first.type());
            }

        },
        MINUS {
            @Override
            public Value perform(Evaluator evaluator, Value first, Value second) {
                var a = first.asNumber();
                var b = second.asNumber();
                return new NumValue(a - b);
            }
        },
        MUL {
            @Override
            public Value perform(Evaluator evaluator, Value first, Value second) {
                var a = first.asNumber();
                var b = second.asNumber();
                return new NumValue(a * b);
            }

        },
        DIV {
            @Override
            public Value perform(Evaluator evaluator, Value first, Value second) {
                var a = first.asNumber();
                var b = second.asNumber();
                return new NumValue(a / b);
            }

        },
        MOD {
            @Override
            public Value perform(Evaluator evaluator, Value first, Value second) {
                var a = first.asNumber();
                var b = second.asNumber();
                return new NumValue(a % b);
            }

        },
        POW {
            @Override
            public Value perform(Evaluator evaluator, Value first, Value second) {
                var a = first.asNumber();
                var b = second.asNumber();
                return new NumValue(Math.pow(a, b));
            }

        },
        AND {
            @Override
            public Value perform(Evaluator evaluator, Value first, Value second) {
                return BoolValue.wrap(first.asBoolean() && second.asBoolean());
            }

            @Override
            public Value performRaw(Evaluator evaluator, Expression<?> first, Expression<?> second) {
                return BoolValue.wrap(evaluator.eval0(first).asBoolean() && evaluator.eval0(second).asBoolean());
            }

        },
        OR {
            @Override
            public Value perform(Evaluator evaluator, Value first, Value second) {
                return BoolValue.wrap(first.asBoolean() || second.asBoolean());
            }

            @Override
            public Value performRaw(Evaluator evaluator, Expression<?> first, Expression<?> second) {
                return BoolValue.wrap(evaluator.eval0(first).asBoolean() ||
                                      evaluator.eval0(second).asBoolean());
            }

        },
        GT {
            @Override
            public Value perform(Evaluator evaluator, Value first, Value second) {
                var a = first.asNumber();
                var b = second.asNumber();
                return BoolValue.wrap(a > b);
            }
        },
        GTE {
            @Override
            public Value perform(Evaluator evaluator, Value first, Value second) {
                var a = first.asNumber();
                var b = second.asNumber();
                return BoolValue.wrap(a >= b);
            }
        },
        LT {
            @Override
            public Value perform(Evaluator evaluator, Value first, Value second) {
                var a = first.asNumber();
                var b = second.asNumber();
                return BoolValue.wrap(a < b);
            }
        },
        LTE {
            @Override
            public Value perform(Evaluator evaluator, Value first, Value second) {
                var a = first.asNumber();
                var b = second.asNumber();
                return BoolValue.wrap(a <= b);
            }

        },
        EQUAL {
            @Override
            public Value perform(Evaluator evaluator, Value first, Value second) {
                return BoolValue.wrap(Objects.equals(first, second));
            }

        };

        public static final BinaryCodec<Op> CODEC = BinaryCodec.enumCodec(values());

        public abstract Value perform(Evaluator evaluator, Value first, Value second);

        public Value performRaw(Evaluator evaluator, Expression<?> first, Expression<?> second) {
            return perform(evaluator, evaluator.eval0(first), evaluator.eval0(second));
        }
    }
}
