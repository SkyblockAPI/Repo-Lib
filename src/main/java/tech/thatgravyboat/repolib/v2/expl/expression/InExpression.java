package tech.thatgravyboat.repolib.v2.expl.expression;

import tech.thatgravyboat.repolib.v2.binary.DecoderContext;
import tech.thatgravyboat.repolib.v2.binary.EncoderContext;
import tech.thatgravyboat.repolib.v2.binary.ExpressionCodec;
import tech.thatgravyboat.repolib.v2.binary.ExpressionTypeRegistry;
import tech.thatgravyboat.repolib.v2.binary.ExpressionTypes;
import tech.thatgravyboat.repolib.v2.binary.NameTable;
import tech.thatgravyboat.repolib.v2.jvm.compiler.CompilationTracker;
import tech.thatgravyboat.repolib.v2.jvm.compiler.Snippets;

import java.io.IOException;
import java.lang.classfile.CodeBuilder;
import java.lang.classfile.Label;
import java.lang.constant.ClassDesc;
import java.lang.constant.MethodTypeDesc;

import static java.lang.constant.ConstantDescs.CD_String;
import static java.lang.constant.ConstantDescs.CD_boolean;
import static tech.thatgravyboat.repolib.v2.jvm.compiler.ExplCD.*;
import static tech.thatgravyboat.repolib.v2.jvm.compiler.ExplCD.CD_Value;

public record InExpression(AccessExpression holder, Expression field) implements Expression {
    @Override
    public ExpressionTypeRegistry.Type<?> expressionId() {
        return ExpressionTypes.IN;
    }

    @Override
    public void precode(NameTable table) {
        table.insert(this.holder);
        table.insert(this.field);
    }

    @Override
    public void encode(EncoderContext buffer) {
        ExpressionCodec.writeUntyped(this.holder, buffer);
        ExpressionCodec.write(this.field, buffer);
    }

    public static InExpression decode(DecoderContext buffer) throws IOException {
        return new InExpression(
                ExpressionCodec.readUntyped(ExpressionTypes.ACCESS, buffer),
                ExpressionCodec.read(buffer)
        );
    }

    @Override
    public boolean compile(CodeBuilder cb, CompilationTracker lc) {
        compileBoolean(cb, lc);
        Label trueCase = cb.newLabel();
        Label falseCase = cb.newLabel();
        cb.ifeq(trueCase);
        cb.getstatic(CD_BoolValue, "TRUE", CD_Value);
        cb.goto_(falseCase);
        cb.labelBinding(trueCase);
        cb.getstatic(CD_BoolValue, "FALSE", CD_Value);
        cb.labelBinding(falseCase);
        cb.nop();
        return false;
    }

    @Override
    public boolean isBoolean() {
        return true;
    }

    @Override
    public void compileBoolean(CodeBuilder cb, CompilationTracker lc) {
        if (field instanceof StrExpression(String value)) {
            cb.loadConstant(value);
        } else {
            field.compile(cb, lc);
            Snippets.getStringOrThrow(cb);
        }

        Label isNotKv = cb.newLabel();
        Label endLabel = cb.newLabel();

        holder.compile(cb, lc);
        cb.dup();
        cb.instanceOf(CD_KeyValue);
        cb.ifeq(isNotKv);

        cb.checkcast(CD_KeyValue);
        cb.swap();
        cb.invokeinterface(CD_KeyValue, "contains", MethodTypeDesc.of(CD_boolean, CD_String));
        cb.goto_(endLabel);

        cb.labelBinding(isNotKv);
        cb.checkcast(CD_StrValue);
        cb.invokevirtual(CD_StrValue, "value", MethodTypeDesc.of(CD_String));
        cb.swap();
        cb.invokevirtual(CD_String, "contains", MethodTypeDesc.of(CD_boolean, ClassDesc.of("java.lang.CharSequence")));

        cb.labelBinding(endLabel);
    }
}
