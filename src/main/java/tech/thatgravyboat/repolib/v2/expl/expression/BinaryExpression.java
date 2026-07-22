package tech.thatgravyboat.repolib.v2.expl.expression;

import tech.thatgravyboat.repolib.v2.expl.Evaluator;
import tech.thatgravyboat.repolib.v2.expl.value.BoolValue;
import tech.thatgravyboat.repolib.v2.expl.value.MutableArrayValue;
import tech.thatgravyboat.repolib.v2.expl.value.NumValue;
import tech.thatgravyboat.repolib.v2.expl.value.StrValue;
import tech.thatgravyboat.repolib.v2.expl.value.Value;

public record BinaryExpression(Op op, Expression first, Expression second) implements SelfEvaluatingExpression {

    @Override
    public Value evaluate(Evaluator evaluator) {
        return op.perform(evaluator, first, second);
    }

    public enum Op {
        PLUS {
            @Override
            Value perform(Evaluator evaluator, Expression first, Expression second) {
                var a = evaluator.eval0(first);
                var b = evaluator.eval0(second);

                if (a instanceof NumValue(double aNum) && b instanceof NumValue(double bNum)) {
                    return new NumValue(aNum + bNum);
                } else if (a instanceof StrValue(String aStr) && b instanceof StrValue(String bStr)) {
                    return new StrValue(aStr + bStr);
                } else if (a instanceof MutableArrayValue array) {
                    array.add(b);
                    return a;
                }

                return evaluator.panic("Unable to add " + b.type() + " to " + a.type());
            }
        },
        MINUS {
            @Override
            Value perform(Evaluator evaluator, Expression first, Expression second) {
                var a = evaluator.getNumberOrThrow(evaluator.eval0(first));
                var b = evaluator.getNumberOrThrow(evaluator.eval0(second));
                return new NumValue(a - b);
            }
        },
        MUL {
            @Override
            Value perform(Evaluator evaluator, Expression first, Expression second) {
                var a = evaluator.getNumberOrThrow(evaluator.eval0(first));
                var b = evaluator.getNumberOrThrow(evaluator.eval0(second));
                return new NumValue(a * b);
            }
        },
        DIV {
            @Override
            Value perform(Evaluator evaluator, Expression first, Expression second) {
                var a = evaluator.getNumberOrThrow(evaluator.eval0(first));
                var b = evaluator.getNumberOrThrow(evaluator.eval0(second));
                return new NumValue(a / b);
            }
        },
        MOD {
            @Override
            Value perform(Evaluator evaluator, Expression first, Expression second) {
                var a = evaluator.getNumberOrThrow(evaluator.eval0(first));
                var b = evaluator.getNumberOrThrow(evaluator.eval0(second));
                return new NumValue(a % b);
            }
        },
        POW {
            @Override
            Value perform(Evaluator evaluator, Expression first, Expression second) {
                var a = evaluator.getNumberOrThrow(evaluator.eval0(first));
                var b = evaluator.getNumberOrThrow(evaluator.eval0(second));
                return new NumValue(Math.pow(a, b));
            }
        },
        AND {
            @Override
            Value perform(Evaluator evaluator, Expression first, Expression second) {
                return BoolValue.wrap(evaluator.getBooleanOrThrow(evaluator.eval0(first)) && evaluator.getBooleanOrThrow(
                        evaluator.eval0(second)));
            }
        },
        OR {
            @Override
            Value perform(Evaluator evaluator, Expression first, Expression second) {
                return BoolValue.wrap(evaluator.getBooleanOrThrow(evaluator.eval0(first)) || evaluator.getBooleanOrThrow(
                        evaluator.eval0(second)));
            }
        },
        GT {
            @Override
            Value perform(Evaluator evaluator, Expression first, Expression second) {
                var a = evaluator.getNumberOrThrow(evaluator.eval0(first));
                var b = evaluator.getNumberOrThrow(evaluator.eval0(second));
                return BoolValue.wrap(a > b);
            }
        },
        GTE {
            @Override
            Value perform(Evaluator evaluator, Expression first, Expression second) {
                var a = evaluator.getNumberOrThrow(evaluator.eval0(first));
                var b = evaluator.getNumberOrThrow(evaluator.eval0(second));
                return BoolValue.wrap(a >= b);
            }
        },
        LT {
            @Override
            Value perform(Evaluator evaluator, Expression first, Expression second) {
                var a = evaluator.getNumberOrThrow(evaluator.eval0(first));
                var b = evaluator.getNumberOrThrow(evaluator.eval0(second));
                return BoolValue.wrap(a < b);
            }
        },
        LTE {
            @Override
            Value perform(Evaluator evaluator, Expression first, Expression second) {
                var a = evaluator.getNumberOrThrow(evaluator.eval0(first));
                var b = evaluator.getNumberOrThrow(evaluator.eval0(second));
                return BoolValue.wrap(a <= b);
            }
        },
        EQUAL {
            @Override
            Value perform(Evaluator evaluator, Expression first, Expression second) {
                var a = evaluator.eval0(first);
                var b = evaluator.eval0(second);
                if (a instanceof StrValue(String aValue) && b instanceof StrValue(String bValue)) {
                    return BoolValue.wrap(aValue == bValue);
                } else if (a instanceof NumValue(double aValue) && b instanceof NumValue(double bValue)) {
                    return BoolValue.wrap(aValue == bValue);
                }
                return evaluator.panic("Dont know how to compare " + a + " and " + b + "!");
            }
        }
        ;

        abstract Value perform(Evaluator evaluator, Expression first, Expression second);
    }
}
