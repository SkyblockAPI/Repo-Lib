package tech.thatgravyboat.repolib.v2.expl.expression;

import org.jetbrains.annotations.NotNull;
import tech.thatgravyboat.repolib.v2.binary.DecoderContext;
import tech.thatgravyboat.repolib.v2.binary.EncoderContext;
import tech.thatgravyboat.repolib.v2.binary.EnumCodec;
import tech.thatgravyboat.repolib.v2.binary.ExpressionCodec;
import tech.thatgravyboat.repolib.v2.binary.ExpressionTypeRegistry;
import tech.thatgravyboat.repolib.v2.binary.ExpressionTypes;
import tech.thatgravyboat.repolib.v2.jvm.compiler.CompilationTracker;
import tech.thatgravyboat.repolib.v2.jvm.compiler.Snippets;
import tech.thatgravyboat.repolib.v2.binary.NameTable;

import java.io.IOException;
import java.lang.classfile.CodeBuilder;
import java.lang.classfile.Label;
import java.lang.constant.MethodTypeDesc;

import static java.lang.constant.ConstantDescs.*;
import static java.lang.constant.ConstantDescs.CD_double;
import static tech.thatgravyboat.repolib.v2.jvm.compiler.ExplCD.*;
import static tech.thatgravyboat.repolib.v2.jvm.compiler.ExplCD.CD_Value;

public record UnaryExpression(Op op, Expression rhs) implements Expression {
    @Override
    public @NotNull String toString() {
        return switch (op) {
            case NEGATE -> "-" + rhs;
            case NOT -> "!" + rhs;
        };
    }

    @Override
    public ExpressionTypeRegistry.Type<?> expressionId() {
        return ExpressionTypes.UNARY;
    }

    @Override
    public void precode(NameTable table) {
        table.insert(this.rhs);
    }

    @Override
    public void encode(EncoderContext buffer) {
        EnumCodec.encode(this.op, buffer);
        ExpressionCodec.write(this.rhs, buffer);
    }

    public static UnaryExpression decode(DecoderContext buffer) throws IOException {
        return new UnaryExpression(
                Op.CODEC.decode(buffer),
                ExpressionCodec.read(buffer)
        );
    }

    @Override
    public boolean compile(CodeBuilder cb, CompilationTracker lc) {
        op.compile(cb, rhs, lc);
        return false;
    }

    @Override
    public boolean isBoolean() {
        return op.isBoolean();
    }

    @Override
    public void compileBoolean(CodeBuilder cb, CompilationTracker lc) {
        op.compileBoolean(cb, rhs, lc);
    }

    public enum Op {
        NEGATE {
            @Override
            void compile(CodeBuilder cb, Expression rhs, CompilationTracker lc) {
                cb.new_(CD_NumValue);
                cb.dup();
                if (rhs instanceof NumExpression(double value)) {
                    cb.loadConstant(-value);
                } else {
                    cb.dconst_0();
                    rhs.compile(cb, lc);
                    Snippets.getNumberOrThrow(cb);
                    cb.dsub();
                }
                cb.invokespecial(CD_NumValue, "<init>", MethodTypeDesc.of(CD_void, CD_double));
            }

            @Override
            boolean isBoolean() {
                return false;
            }
        }, NOT {
            @Override
            void compile(CodeBuilder cb, Expression rhs, CompilationTracker lc) {
                if (rhs.isBoolean()) {
                    rhs.compileBoolean(cb, lc);
                } else {
                    cb.aload(1);
                    rhs.compile(cb, lc);
                    cb.invokevirtual(CD_Evaluator, "asBool", MethodTypeDesc.of(CD_boolean, CD_Value));
                }
                Label meow1 = cb.newLabel();
                Label meow2 = cb.newLabel();
                cb.ifne(meow1);
                cb.getstatic(CD_BoolValue, "TRUE", CD_Value);
                cb.goto_(meow2);
                cb.labelBinding(meow1);
                cb.getstatic(CD_BoolValue, "FALSE", CD_Value);
                cb.labelBinding(meow2);
            }

            @Override
            boolean isBoolean() {
                return true;
            }

            @Override
            void compileBoolean(CodeBuilder cb, Expression rhs, CompilationTracker lc) {
                if (rhs.isBoolean()) {
                    rhs.compileBoolean(cb, lc);
                } else {
                    cb.aload(1);
                    rhs.compile(cb, lc);
                    cb.invokevirtual(CD_Evaluator, "asBool", MethodTypeDesc.of(CD_boolean, CD_Value));
                }
                Label meow1 = cb.newLabel();
                Label meow2 = cb.newLabel();
                cb.ifne(meow1);
                cb.loadConstant(1);
                cb.goto_(meow2);
                cb.labelBinding(meow1);
                cb.loadConstant(0);
                cb.labelBinding(meow2);
            }
        },
        ;

        abstract void compile(CodeBuilder cb, Expression rhs, CompilationTracker lc);

        abstract boolean isBoolean();
        void compileBoolean(CodeBuilder cb, Expression rhs, CompilationTracker lc) {}

        public static final EnumCodec<Op> CODEC = new EnumCodec<>(values());
    }
}
