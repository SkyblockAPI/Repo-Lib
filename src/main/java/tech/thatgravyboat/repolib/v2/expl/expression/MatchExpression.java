package tech.thatgravyboat.repolib.v2.expl.expression;

import java.io.IOException;
import java.util.Collection;
import tech.thatgravyboat.repolib.v2.binary.DecoderContext;
import tech.thatgravyboat.repolib.v2.binary.Encodable;
import tech.thatgravyboat.repolib.v2.binary.EncoderContext;
import tech.thatgravyboat.repolib.v2.binary.EnumCodec;
import tech.thatgravyboat.repolib.v2.binary.ExpressionCodec;
import tech.thatgravyboat.repolib.v2.binary.ExpressionTypeRegistry;
import tech.thatgravyboat.repolib.v2.binary.ExpressionTypes;
import tech.thatgravyboat.repolib.v2.binary.NameTable;
import tech.thatgravyboat.repolib.v2.expl.Evaluator;
import tech.thatgravyboat.repolib.v2.expl.value.Value;

public record MatchExpression(Expression value, Collection<MatchBranch> branches) implements SelfEvaluatingExpression {
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
    public ExpressionTypeRegistry.Type<?> expressionId() {
        return ExpressionTypes.MATCH;
    }

    @Override
    public void precode(NameTable table) {
        table.insert(this.value);
        for (var branch : branches) {
            table.insert(branch);
        }
    }

    @Override
    public void encode(EncoderContext buffer) {
        ExpressionCodec.write(this.value, buffer);
        buffer.writeCollection(this.branches, MatchBranch::encode);
    }

    public static MatchExpression decode(DecoderContext buffer) throws IOException {
        return new MatchExpression(ExpressionCodec.read(buffer), buffer.readCollection(MatchBranch::decode));
    }

    public record MatchBranch(MatchCondition condition, Expression check, Expression branch) implements Encodable {
        @Override
        public void encode(EncoderContext buffer) {
            EnumCodec.encode(this.condition, buffer);
            ExpressionCodec.writeNullable(this.check, buffer);
            ExpressionCodec.write(this.branch, buffer);
        }

        @Override
        public void precode(NameTable table) {
            table.insert(this.check);
            table.insert(this.branch);
        }

        public static MatchBranch decode(DecoderContext buffer) throws IOException {
            return new MatchBranch(
                MatchCondition.CODEC.decode(buffer),
                ExpressionCodec.readNullable(buffer),
                ExpressionCodec.read(buffer));
        }
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

        public static final EnumCodec<MatchCondition> CODEC = new EnumCodec<>(values());


        public abstract boolean compare(Evaluator evaluator, Value value, Value testValue);
    }
}
