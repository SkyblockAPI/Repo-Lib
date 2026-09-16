package tech.thatgravyboat.repolib.v2.expl.expression;

import tech.thatgravyboat.repolib.v2.binary.ByteBuffer;
import tech.thatgravyboat.repolib.v2.binary.Encodable;
import tech.thatgravyboat.repolib.v2.binary.EnumCodec;
import tech.thatgravyboat.repolib.v2.binary.ExpressionCodec;
import tech.thatgravyboat.repolib.v2.binary.ExpressionTypeRegistry;
import tech.thatgravyboat.repolib.v2.binary.ExpressionTypes;
import tech.thatgravyboat.repolib.v2.expl.Evaluator;
import tech.thatgravyboat.repolib.v2.expl.value.BoolValue;
import tech.thatgravyboat.repolib.v2.expl.value.MutableArrayValue;
import tech.thatgravyboat.repolib.v2.expl.value.NumValue;
import tech.thatgravyboat.repolib.v2.expl.value.StrValue;
import tech.thatgravyboat.repolib.v2.expl.value.Value;

import java.io.IOException;
import java.util.Objects;

public record BinaryExpression(Op op, Expression first, Expression second) implements SelfEvaluatingExpression {

    @Override
    public Value evaluate(Evaluator evaluator) {
        return op.perform(evaluator, first, second);
    }

    @Override
    public ExpressionTypeRegistry.Type<?> expressionId() {
        return ExpressionTypes.BINARY;
    }

    @Override
    public void encode(ByteBuffer buffer) {
        EnumCodec.encode(this.op, buffer);
        ExpressionCodec.write(this.first, buffer);
        ExpressionCodec.write(this.second, buffer);
    }

    public static BinaryExpression decode(ByteBuffer buffer) throws IOException {
        return new BinaryExpression(
                Op.CODEC.decode(buffer),
                ExpressionCodec.read(buffer),
                ExpressionCodec.read(buffer)
        );
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
        }, MINUS {
            @Override
            public Value perform(Evaluator evaluator, Value first, Value second) {
                var a = evaluator.getNumberOrThrow(first);
                var b = evaluator.getNumberOrThrow(second);
                return new NumValue(a - b);
            }
        }, MUL {
            @Override
            public Value perform(Evaluator evaluator, Value first, Value second) {
                var a = evaluator.getNumberOrThrow(first);
                var b = evaluator.getNumberOrThrow(second);
                return new NumValue(a * b);
            }
        }, DIV {
            @Override
            public Value perform(Evaluator evaluator, Value first, Value second) {
                var a = evaluator.getNumberOrThrow(first);
                var b = evaluator.getNumberOrThrow(second);
                return new NumValue(a / b);
            }
        }, MOD {
            @Override
            public Value perform(Evaluator evaluator, Value first, Value second) {
                var a = evaluator.getNumberOrThrow(first);
                var b = evaluator.getNumberOrThrow(second);
                return new NumValue(a % b);
            }
        }, POW {
            @Override
            public Value perform(Evaluator evaluator, Value first, Value second) {
                var a = evaluator.getNumberOrThrow(first);
                var b = evaluator.getNumberOrThrow(second);
                return new NumValue(Math.pow(a, b));
            }
        }, AND {
            @Override
            public Value perform(Evaluator evaluator, Value first, Value second) {
                return BoolValue.wrap(evaluator.getBooleanOrThrow(first) && evaluator.getBooleanOrThrow(second));
            }

            @Override
            public Value perform(Evaluator evaluator, Expression first, Expression second) {
                return BoolValue.wrap(evaluator.getBooleanOrThrow(evaluator.eval0(first)) && evaluator.getBooleanOrThrow(
                        evaluator.eval0(second)));
            }
        }, OR {
            @Override
            public Value perform(Evaluator evaluator, Value first, Value second) {
                return BoolValue.wrap(evaluator.getBooleanOrThrow(first) || evaluator.getBooleanOrThrow(second));
            }

            @Override
            public Value perform(Evaluator evaluator, Expression first, Expression second) {
                return BoolValue.wrap(evaluator.getBooleanOrThrow(evaluator.eval0(first)) || evaluator.getBooleanOrThrow(
                        evaluator.eval0(second)));
            }
        }, GT {
            @Override
            public Value perform(Evaluator evaluator, Value first, Value second) {
                var a = evaluator.getNumberOrThrow(first);
                var b = evaluator.getNumberOrThrow(second);
                return BoolValue.wrap(a > b);
            }
        }, GTE {
            @Override
            public Value perform(Evaluator evaluator, Value first, Value second) {
                var a = evaluator.getNumberOrThrow(first);
                var b = evaluator.getNumberOrThrow(second);
                return BoolValue.wrap(a >= b);
            }
        }, LT {
            @Override
            public Value perform(Evaluator evaluator, Value first, Value second) {
                var a = evaluator.getNumberOrThrow(first);
                var b = evaluator.getNumberOrThrow(second);
                return BoolValue.wrap(a < b);
            }
        }, LTE {
            @Override
            public Value perform(Evaluator evaluator, Value first, Value second) {
                var a = evaluator.getNumberOrThrow(first);
                var b = evaluator.getNumberOrThrow(second);
                return BoolValue.wrap(a <= b);
            }
        }, EQUAL {
            @Override
            public Value perform(Evaluator evaluator, Value first, Value second) {
                return BoolValue.wrap(Objects.equals(first, second));
            }
        };

        public static final EnumCodec<Op> CODEC = new EnumCodec<>(values());

        public abstract Value perform(Evaluator evaluator, Value first, Value second);

        public Value perform(Evaluator evaluator, Expression first, Expression second) {
            return perform(evaluator, evaluator.eval0(first), evaluator.eval0(second));
        }
    }
}
