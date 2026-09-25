package tech.thatgravyboat.repolib.v2.expl.expression;

import java.util.Collection;
import org.jetbrains.annotations.Nullable;
import tech.thatgravyboat.repolib.v2.binary.BinaryCodec;
import tech.thatgravyboat.repolib.v2.binary.BinaryRecordBuilder;
import tech.thatgravyboat.repolib.v2.binary.ExpressionTypeRegistry;
import tech.thatgravyboat.repolib.v2.binary.ExpressionTypes;
import tech.thatgravyboat.repolib.v2.expl.Evaluator;
import tech.thatgravyboat.repolib.v2.expl.value.Value;

public record MatchExpression(Expression<?> value, Collection<MatchBranch> branches)
    implements SelfEvaluatingExpression<MatchExpression> {

    public static final BinaryCodec<MatchExpression> CODEC = BinaryRecordBuilder.of(
        BinaryCodec.EXPRESSION.forGetter(MatchExpression::value),
        MatchBranch.CODEC.collection().forGetter(MatchExpression::branches),
        MatchExpression::new);

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

    @Override
    public boolean requiresSemicolon() {
        return false;
    }

    @Override
    public ExpressionTypeRegistry.Type<MatchExpression> expressionId() {
        return ExpressionTypes.MATCH;
    }

    public record MatchBranch(MatchCondition condition, @Nullable Expression<?> check, Expression<?> branch) {
        public static final BinaryCodec<MatchBranch> CODEC = BinaryRecordBuilder.of(
            MatchCondition.CODEC.forGetter(MatchBranch::condition),
            BinaryCodec.EXPRESSION.nullable().forGetter(MatchBranch::check),
            BinaryCodec.EXPRESSION.forGetter(MatchBranch::branch),
            MatchBranch::new);
    }

    public enum MatchCondition {
        EQUALS {
            @Override
            public boolean compare(Evaluator evaluator, Value value, Value testValue) {
                return value.equals(testValue);
            }
        },
        ELSE {
            @Override
            public boolean compare(Evaluator evaluator, Value value, Value testValue) {
                return true;
            }
        },
        LT {
            @Override
            public boolean compare(Evaluator evaluator, Value value, Value testValue) {
                var first = evaluator.getNumberOrThrow(value);
                var second = evaluator.getNumberOrThrow(testValue);
                return first < second;
            }
        },
        GT {
            @Override
            public boolean compare(Evaluator evaluator, Value value, Value testValue) {
                var first = evaluator.getNumberOrThrow(value);
                var second = evaluator.getNumberOrThrow(testValue);
                return first > second;
            }
        },
        LTE {
            @Override
            public boolean compare(Evaluator evaluator, Value value, Value testValue) {
                var first = evaluator.getNumberOrThrow(value);
                var second = evaluator.getNumberOrThrow(testValue);
                return first <= second;
            }
        },
        GTE {
            @Override
            public boolean compare(Evaluator evaluator, Value value, Value testValue) {
                var first = evaluator.getNumberOrThrow(value);
                var second = evaluator.getNumberOrThrow(testValue);
                return first >= second;
            }
        },
        ;

        public static final BinaryCodec<MatchCondition> CODEC = BinaryCodec.enumCodec(values());


        public abstract boolean compare(Evaluator evaluator, Value value, Value testValue);
    }
}
