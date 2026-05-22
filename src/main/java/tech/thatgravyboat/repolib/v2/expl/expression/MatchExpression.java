package tech.thatgravyboat.repolib.v2.expl.expression;

import tech.thatgravyboat.repolib.v2.expl.Evaluator;
import tech.thatgravyboat.repolib.v2.expl.value.Value;

import java.util.List;

public record MatchExpression(Expression value, List<MatchBranch> branches) implements SelfEvaluatingExpression {
    @Override
    public Value evaluate(Evaluator evaluator) {
        var value = evaluator.eval0(this.value);

        for (var branch : branches) {
            if (branch.condition.compare(evaluator, value, evaluator.eval0(branch.check))) {
                return evaluator.eval0(branch.branch);
            }
        }

        return Value.NIL;
    }

    public record MatchBranch(MatchCondition condition, Expression check, Expression branch) {}

    public enum MatchCondition {
        EQUALS {
            @Override
            boolean compare(Evaluator evaluator, Value value, Value testValue) {
                return value.equals(testValue);
            }
        },
        ELSE {
            @Override
            boolean compare(Evaluator evaluator, Value value, Value testValue) {
                return true;
            }
        },
        LT {
            @Override
            boolean compare(Evaluator evaluator, Value value, Value testValue) {
                var first = evaluator.getNumberOrThrow(value);
                var second = evaluator.getNumberOrThrow(testValue);
                return first < second;
            }
        },
        GT {
            @Override
            boolean compare(Evaluator evaluator, Value value, Value testValue) {
                var first = evaluator.getNumberOrThrow(value);
                var second = evaluator.getNumberOrThrow(testValue);
                return first > second;
            }
        },
        LTE {
            @Override
            boolean compare(Evaluator evaluator, Value value, Value testValue) {
                var first = evaluator.getNumberOrThrow(value);
                var second = evaluator.getNumberOrThrow(testValue);
                return first <= second;
            }
        },
        GTE {
            @Override
            boolean compare(Evaluator evaluator, Value value, Value testValue) {
                var first = evaluator.getNumberOrThrow(value);
                var second = evaluator.getNumberOrThrow(testValue);
                return first >= second;
            }
        },
        ;

        abstract boolean compare(Evaluator evaluator, Value value, Value testValue);
    }
}
