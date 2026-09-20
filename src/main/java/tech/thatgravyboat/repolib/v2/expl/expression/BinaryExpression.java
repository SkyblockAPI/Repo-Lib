package tech.thatgravyboat.repolib.v2.expl.expression;

import tech.thatgravyboat.repolib.v2.binary.DecoderContext;
import tech.thatgravyboat.repolib.v2.binary.EncoderContext;
import tech.thatgravyboat.repolib.v2.binary.EnumCodec;
import tech.thatgravyboat.repolib.v2.binary.ExpressionCodec;
import tech.thatgravyboat.repolib.v2.binary.ExpressionTypeRegistry;
import tech.thatgravyboat.repolib.v2.binary.ExpressionTypes;
import tech.thatgravyboat.repolib.v2.binary.NameTable;
import tech.thatgravyboat.repolib.v2.expl.Evaluator;
import tech.thatgravyboat.repolib.v2.expl.value.BoolValue;
import tech.thatgravyboat.repolib.v2.expl.value.MutableArrayValue;
import tech.thatgravyboat.repolib.v2.expl.value.NumValue;
import tech.thatgravyboat.repolib.v2.expl.value.StrValue;
import tech.thatgravyboat.repolib.v2.expl.value.Value;
import tech.thatgravyboat.repolib.v2.jvm.compiler.CompilationTracker;
import tech.thatgravyboat.repolib.v2.jvm.compiler.Snippets;

import java.io.IOException;
import java.lang.classfile.CodeBuilder;
import java.lang.classfile.Label;
import java.lang.constant.ClassDesc;
import java.lang.constant.MethodTypeDesc;
import java.util.Objects;

import static java.lang.constant.ConstantDescs.*;
import static tech.thatgravyboat.repolib.v2.jvm.compiler.ExplCD.*;
import static tech.thatgravyboat.repolib.v2.jvm.compiler.ExplCD.CD_BoolValue;

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
    public void encode(EncoderContext buffer) {
        EnumCodec.encode(this.op, buffer);
        ExpressionCodec.write(this.first, buffer);
        ExpressionCodec.write(this.second, buffer);
    }

    @Override
    public void precode(NameTable table) {
        this.first.precode(table);
        this.second.precode(table);
    }

    public static BinaryExpression decode(DecoderContext buffer) throws IOException {
        return new BinaryExpression(
                Op.CODEC.decode(buffer),
                ExpressionCodec.read(buffer),
                ExpressionCodec.read(buffer)
        );
    }

    @Override
    public boolean compile(CodeBuilder cb, CompilationTracker lc) {
        op.compile(cb, first, second, lc);
        return false;
    }

    @Override
    public boolean isBoolean() {
        return op.isBoolean();
    }

    @Override
    public boolean isNumber() {
        return op.isNumber(first, second);
    }

    @Override
    public void compileBoolean(CodeBuilder cb, CompilationTracker lc) {
        op.compileBoolean(cb, first, second, lc);
    }

    @Override
    public void compileNumber(CodeBuilder cb, CompilationTracker lc) {
        op.compileNumber(cb, first, second, lc);
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

            @Override
            public void compile(CodeBuilder cb, Expression first, Expression second, CompilationTracker lc) {
                if (first instanceof StrExpression(String firstString) && second instanceof StrExpression(String secondString)) {
                    Snippets.loadStrValue(cb, firstString + secondString);
                } else if (first instanceof NumExpression(double firstValue) && second instanceof NumExpression(double secondValue)) {
                    Snippets.loadNumValue(cb, firstValue + secondValue);
                } else if (first.isNumber() && second.isNumber()) {
                    cb.new_(CD_NumValue);
                    cb.dup();
                    compileNumber(cb, first, second, lc);
                    cb.invokespecial(CD_NumValue, "<init>", MethodTypeDesc.of(CD_void, CD_double));
                } else {
                    super.compile(cb, first, second, lc);
                }
            }

            @Override
            public boolean isNumber(Expression first, Expression second) {
                return first.isNumber() && second.isNumber();
            }

            @Override
            public void compileNumber(CodeBuilder cb, Expression first, Expression second, CompilationTracker lc) {
                first.compileNumberElseConvert(cb, lc);
                second.compileNumberElseConvert(cb, lc);
                cb.dadd();
            }
        }, MINUS {
            @Override
            public Value perform(Evaluator evaluator, Value first, Value second) {
                var a = evaluator.getNumberOrThrow(first);
                var b = evaluator.getNumberOrThrow(second);
                return new NumValue(a - b);
            }

            @Override
            public boolean isNumber(Expression first, Expression second) {
                return true;
            }

            @Override
            public void compileNumber(CodeBuilder cb, Expression first, Expression second, CompilationTracker lc) {
                first.compileNumberElseConvert(cb, lc);
                second.compileNumberElseConvert(cb, lc);
                cb.dsub();
            }
        }, MUL {
            @Override
            public Value perform(Evaluator evaluator, Value first, Value second) {
                var a = evaluator.getNumberOrThrow(first);
                var b = evaluator.getNumberOrThrow(second);
                return new NumValue(a * b);
            }

            @Override
            public boolean isNumber(Expression first, Expression second) {
                return true;
            }

            @Override
            public void compileNumber(CodeBuilder cb, Expression first, Expression second, CompilationTracker lc) {
                first.compileNumberElseConvert(cb, lc);
                second.compileNumberElseConvert(cb, lc);
                cb.dmul();
            }
        }, DIV {
            @Override
            public Value perform(Evaluator evaluator, Value first, Value second) {
                var a = evaluator.getNumberOrThrow(first);
                var b = evaluator.getNumberOrThrow(second);
                return new NumValue(a / b);
            }

            @Override
            public boolean isNumber(Expression first, Expression second) {
                return true;
            }

            @Override
            public void compileNumber(CodeBuilder cb, Expression first, Expression second, CompilationTracker lc) {
                first.compileNumberElseConvert(cb, lc);
                second.compileNumberElseConvert(cb, lc);
                cb.ddiv();
            }
        }, MOD {
            @Override
            public Value perform(Evaluator evaluator, Value first, Value second) {
                var a = evaluator.getNumberOrThrow(first);
                var b = evaluator.getNumberOrThrow(second);
                return new NumValue(a % b);
            }

            @Override
            public boolean isNumber(Expression first, Expression second) {
                return true;
            }

            @Override
            public void compileNumber(CodeBuilder cb, Expression first, Expression second, CompilationTracker lc) {
                first.compileNumberElseConvert(cb, lc);
                second.compileNumberElseConvert(cb, lc);
                cb.drem();
            }
        }, POW {
            @Override
            public Value perform(Evaluator evaluator, Value first, Value second) {
                var a = evaluator.getNumberOrThrow(first);
                var b = evaluator.getNumberOrThrow(second);
                return new NumValue(Math.pow(a, b));
            }

            @Override
            public boolean isNumber(Expression first, Expression second) {
                return true;
            }

            @Override
            public void compileNumber(CodeBuilder cb, Expression first, Expression second, CompilationTracker lc) {
                first.compileNumberElseConvert(cb, lc);
                second.compileNumberElseConvert(cb, lc);
                cb.invokestatic(ClassDesc.of("java.lang.Math"), "pow", MethodTypeDesc.of(CD_double, CD_double, CD_double));
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

            private void compileAnd(CodeBuilder cb, Expression first, Expression second, CompilationTracker lc, Label falseLabel) {
                first.compileBooleanElseConvert(cb, lc);
                cb.ifeq(falseLabel);

                second.compileBooleanElseConvert(cb,lc);
                cb.ifeq(falseLabel);
            }

            @Override
            public void compile(CodeBuilder cb, Expression first, Expression second, CompilationTracker lc) {
                Label falseLabel = cb.newLabel();
                Label endLabel = cb.newLabel();
                compileAnd(cb, first, second, lc, falseLabel);
                cb.getstatic(CD_BoolValue, "TRUE", CD_Value);
                cb.goto_(endLabel);
                cb.labelBinding(falseLabel);
                cb.getstatic(CD_BoolValue, "FALSE", CD_Value);
                cb.labelBinding(endLabel);
            }

            @Override
            public boolean isBoolean() {
                return true;
            }

            @Override
            public void compileBoolean(CodeBuilder cb, Expression first, Expression second, CompilationTracker lc) {
                Label falseLabel = cb.newLabel();
                Label endLabel = cb.newLabel();
                compileAnd(cb, first, second, lc, falseLabel);
                cb.loadConstant(1);
                cb.goto_(endLabel);
                cb.labelBinding(falseLabel);
                cb.loadConstant(0);
                cb.labelBinding(endLabel);
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

            private void compileOr(CodeBuilder cb, Expression first, Expression second, CompilationTracker lc, Label falseLabel, Label trueLabel) {
                first.compileBooleanElseConvert(cb, lc);
                cb.ifne(trueLabel);
                second.compileBooleanElseConvert(cb, lc);
                cb.ifeq(falseLabel);
            }

            @Override
            public void compile(CodeBuilder cb, Expression first, Expression second, CompilationTracker lc) {
                Label falseLabel = cb.newLabel();
                Label trueLabel = cb.newLabel();
                Label endLabel = cb.newLabel();
                compileOr(cb, first, second, lc, falseLabel, trueLabel);
                cb.labelBinding(trueLabel);
                cb.getstatic(CD_BoolValue, "TRUE", CD_Value);
                cb.goto_(endLabel);
                cb.labelBinding(falseLabel);
                cb.getstatic(CD_BoolValue, "FALSE", CD_Value);
                cb.labelBinding(endLabel);
            }

            @Override
            public boolean isBoolean() {
                return true;
            }

            @Override
            public void compileBoolean(CodeBuilder cb, Expression first, Expression second, CompilationTracker lc) {
                Label falseLabel = cb.newLabel();
                Label trueLabel = cb.newLabel();
                Label endLabel = cb.newLabel();
                compileOr(cb, first, second, lc, falseLabel, trueLabel);
                cb.labelBinding(trueLabel);
                cb.loadConstant(1);
                cb.goto_(endLabel);
                cb.labelBinding(falseLabel);
                cb.loadConstant(0);
                cb.labelBinding(endLabel);
            }
        }, GT {
            @Override
            public Value perform(Evaluator evaluator, Value first, Value second) {
                var a = evaluator.getNumberOrThrow(first);
                var b = evaluator.getNumberOrThrow(second);
                return BoolValue.wrap(a > b);
            }

            @Override
            public boolean isBoolean() {
                return true;
            }
            @Override
            public void compileBoolean(CodeBuilder cb, Expression first, Expression second, CompilationTracker lc) {
                first.compileNumberElseConvert(cb, lc);
                second.compileNumberElseConvert(cb, lc);
                cb.dcmpl();
                Label falseLabel = cb.newLabel();
                Label endLabel = cb.newLabel();
                cb.ifle(falseLabel);
                cb.loadConstant(1);
                cb.goto_(endLabel);
                cb.labelBinding(falseLabel);
                cb.loadConstant(0);
                cb.labelBinding(endLabel);
            }
        }, GTE {
            @Override
            public Value perform(Evaluator evaluator, Value first, Value second) {
                var a = evaluator.getNumberOrThrow(first);
                var b = evaluator.getNumberOrThrow(second);
                return BoolValue.wrap(a >= b);
            }

            @Override
            public boolean isBoolean() {
                return true;
            }
            @Override
            public void compileBoolean(CodeBuilder cb, Expression first, Expression second, CompilationTracker lc) {
                first.compileNumberElseConvert(cb, lc);
                second.compileNumberElseConvert(cb, lc);
                cb.dcmpl();
                Label falseLabel = cb.newLabel();
                Label endLabel = cb.newLabel();
                cb.iflt(falseLabel);
                cb.loadConstant(1);
                cb.goto_(endLabel);
                cb.labelBinding(falseLabel);
                cb.loadConstant(0);
                cb.labelBinding(endLabel);
            }
        }, LT {
            @Override
            public Value perform(Evaluator evaluator, Value first, Value second) {
                var a = evaluator.getNumberOrThrow(first);
                var b = evaluator.getNumberOrThrow(second);
                return BoolValue.wrap(a < b);
            }
            @Override
            public boolean isBoolean() {
                return true;
            }
            @Override
            public void compileBoolean(CodeBuilder cb, Expression first, Expression second, CompilationTracker lc) {
                first.compileNumberElseConvert(cb, lc);
                second.compileNumberElseConvert(cb, lc);
                cb.dcmpg();
                Label falseLabel = cb.newLabel();
                Label endLabel = cb.newLabel();
                cb.ifge(falseLabel);
                cb.loadConstant(1);
                cb.goto_(endLabel);
                cb.labelBinding(falseLabel);
                cb.loadConstant(0);
                cb.labelBinding(endLabel);
            }
        }, LTE {
            @Override
            public Value perform(Evaluator evaluator, Value first, Value second) {
                var a = evaluator.getNumberOrThrow(first);
                var b = evaluator.getNumberOrThrow(second);
                return BoolValue.wrap(a <= b);
            }
            @Override
            public boolean isBoolean() {
                return true;
            }
            @Override
            public void compileBoolean(CodeBuilder cb, Expression first, Expression second, CompilationTracker lc) {
                first.compileNumberElseConvert(cb, lc);
                second.compileNumberElseConvert(cb, lc);
                cb.dcmpg();
                Label falseLabel = cb.newLabel();
                Label endLabel = cb.newLabel();
                cb.ifgt(falseLabel);
                cb.loadConstant(1);
                cb.goto_(endLabel);
                cb.labelBinding(falseLabel);
                cb.loadConstant(0);
                cb.labelBinding(endLabel);
            }
        }, EQUAL {
            @Override
            public Value perform(Evaluator evaluator, Value first, Value second) {
                return BoolValue.wrap(Objects.equals(first, second));
            }

            @Override
            public void compile(CodeBuilder cb, Expression first, Expression second, CompilationTracker lc) {
                this.compileBoolean(cb, first, second, lc);
                Snippets.booleanToBoolValue(cb);
            }

            @Override
            public boolean isBoolean() {
                return true;
            }

            @Override
            public void compileBoolean(CodeBuilder cb, Expression first, Expression second, CompilationTracker lc) {
                first.compile(cb, lc);
                second.compile(cb, lc);
                cb.invokevirtual(CD_Object, "equals", MethodTypeDesc.of(CD_boolean, CD_Object));
            }
        };

        public static final EnumCodec<Op> CODEC = new EnumCodec<>(values());

        public abstract Value perform(Evaluator evaluator, Value first, Value second);

        public Value perform(Evaluator evaluator, Expression first, Expression second) {
            return perform(evaluator, evaluator.eval0(first), evaluator.eval0(second));
        }

        public void compile(CodeBuilder cb, Expression first, Expression second, CompilationTracker lc) {
            Enum.EnumDesc<BinaryExpression.Op> enumDescription = describeConstable().orElseThrow();
            cb.loadConstant(enumDescription);
            cb.aload(1);
            first.compile(cb, lc);
            second.compile(cb, lc);
            cb.invokevirtual(
                    enumDescription.constantType(),
                    "perform",
                    MethodTypeDesc.of(CD_Value, CD_Evaluator, CD_Value, CD_Value)
            );
            describeConstable().orElseThrow();
        }

        public boolean isBoolean() {
            return false;
        }
        public void compileBoolean(CodeBuilder cb, Expression first, Expression second, CompilationTracker lc) {}
        public boolean isNumber(Expression first, Expression second) {
            return false;
        }
        public void compileNumber(CodeBuilder cb, Expression first, Expression second, CompilationTracker lc) {}
    }
}
