package tech.thatgravyboat.repolib.v2.expl.expression;

import tech.thatgravyboat.repolib.v2.binary.ByteBuffer;
import tech.thatgravyboat.repolib.v2.binary.Encodable;
import tech.thatgravyboat.repolib.v2.binary.EnumCodec;
import tech.thatgravyboat.repolib.v2.binary.ExpressionCodec;
import tech.thatgravyboat.repolib.v2.binary.ExpressionTypeRegistry;
import tech.thatgravyboat.repolib.v2.binary.ExpressionTypes;
import tech.thatgravyboat.repolib.v2.expl.Evaluator;
import tech.thatgravyboat.repolib.v2.expl.value.Value;
import tech.thatgravyboat.repolib.v2.jvm.compiler.CompilationTracker;
import tech.thatgravyboat.repolib.v2.jvm.compiler.Snippets;

import java.io.IOException;
import java.lang.classfile.CodeBuilder;
import java.lang.classfile.Label;
import java.lang.constant.MethodTypeDesc;
import java.util.Collection;

import static java.lang.constant.ConstantDescs.CD_boolean;
import static tech.thatgravyboat.repolib.v2.jvm.compiler.ExplCD.CD_Evaluator;
import static tech.thatgravyboat.repolib.v2.jvm.compiler.ExplCD.CD_Value;

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
    public void encode(ByteBuffer buffer) {
        ExpressionCodec.write(this.value, buffer);
        buffer.writeCollection(this.branches, MatchBranch::encode);
    }

    public static MatchExpression decode(ByteBuffer buffer) throws IOException {
        return new MatchExpression(
                ExpressionCodec.read(buffer),
                buffer.readCollection(MatchBranch::decode)
        );
    }

    public record MatchBranch(MatchCondition condition, Expression check, Expression branch) implements Encodable{
        @Override
        public void encode(ByteBuffer buffer) {
            EnumCodec.encode(this.condition, buffer);
            ExpressionCodec.writeNullable(this.check, buffer);
            ExpressionCodec.write(this.branch, buffer);
        }

        public static MatchBranch decode(ByteBuffer buffer) throws IOException {
            return new MatchBranch(
                    MatchCondition.CODEC.decode(buffer),
                    ExpressionCodec.readNullable(buffer),
                    ExpressionCodec.read(buffer)
            );
        }
    }

    @Override
    public boolean compile(CodeBuilder cb, CompilationTracker lc) {
        value.compile(cb, lc);
        int localSlot = lc.getLowestUnused();
        cb.astore(localSlot);
        Label endEndLabel = cb.newLabel();
        boolean hasCatchall = false;
        for (MatchExpression.MatchBranch branch : branches) {
            Label endLabel = cb.newLabel();
            if (branch.check() != null) {
                Enum.EnumDesc<MatchExpression.MatchCondition> meow = branch.condition().describeConstable().orElseThrow();
                cb.loadConstant(meow);
                cb.aload(1);
                cb.aload(localSlot);
                branch.check().compile(cb, lc);
                cb.invokevirtual(meow.constantType(), "compare", MethodTypeDesc.of(CD_boolean, CD_Evaluator, CD_Value, CD_Value));
                cb.ifeq(endLabel);
            } else {
                hasCatchall = true;
            }

            branch.branch().compile(cb, lc);
            cb.goto_(endEndLabel);
            cb.labelBinding(endLabel);
        }
        if (!hasCatchall) {
            Snippets.pushNil(cb);
        }
        lc.free(localSlot);
        cb.labelBinding(endEndLabel);
        cb.nop();
        return false;
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
