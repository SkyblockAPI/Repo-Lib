package tech.thatgravyboat.repolib.v2.expl.expression;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import tech.thatgravyboat.repolib.v2.binary.ByteBuffer;
import tech.thatgravyboat.repolib.v2.binary.ExpressionCodec;
import tech.thatgravyboat.repolib.v2.binary.ExpressionTypeRegistry;
import tech.thatgravyboat.repolib.v2.binary.ExpressionTypes;
import tech.thatgravyboat.repolib.v2.jvm.compiler.CompilationTracker;
import tech.thatgravyboat.repolib.v2.jvm.compiler.Snippets;

import java.io.IOException;
import java.lang.classfile.CodeBuilder;
import java.lang.classfile.Label;
import java.lang.constant.MethodTypeDesc;

import static java.lang.constant.ConstantDescs.*;
import static tech.thatgravyboat.repolib.v2.jvm.compiler.ExplCD.*;

public record AccessExpression(@Nullable Expression lhs, Expression field) implements Expression {

    @Override
    public @NotNull String toString() {
        return lhs + "." + field;
    }

    public static AccessExpression decode(ByteBuffer buffer) throws IOException {
        return new AccessExpression(
                ExpressionCodec.readNullable(buffer),
                ExpressionCodec.read(buffer)
        );
    }

    @Override
    public ExpressionTypeRegistry.Type<?> expressionId() {
        return ExpressionTypes.ACCESS;
    }

    @Override
    public void encode(ByteBuffer buffer) {
        ExpressionCodec.writeNullable(this.lhs, buffer);
        ExpressionCodec.write(this.field, buffer);
    }

    @Override
    public boolean compile(CodeBuilder cb, CompilationTracker lc) {
        if (lhs == null) {
            cb.aload(1);
            if (field instanceof StrExpression(String value)) {
                cb.loadConstant(value);
            } else {
                field.compile(cb, lc);
                Snippets.getStringOrThrow(cb);
            }
            cb.invokevirtual(CD_Evaluator, "getField", MethodTypeDesc.of(CD_Value, CD_String));
        } else {
            if (field instanceof StrExpression(String value)) {
                cb.aload(1);
                lhs.compile(cb, lc);
                cb.loadConstant(value);
                cb.invokevirtual(CD_Evaluator, "getField", MethodTypeDesc.of(CD_Value, CD_Value, CD_String));
            } else {
                Label arrayValLabel = cb.newLabel();
                Label endLabel = cb.newLabel();
                lhs.compile(cb, lc);
                cb.checkcast(CD_KeyValue);
                field.compile(cb, lc);
                cb.dup();
                cb.instanceOf(CD_NumValue);
                cb.ifne(arrayValLabel);
                Snippets.getStringOrThrow(cb);
                cb.invokeinterface(CD_KeyValue, "get", MethodTypeDesc.of(CD_Value, CD_String));
                cb.goto_(endLabel);
                cb.labelBinding(arrayValLabel);
                Snippets.getNumberOrThrow(cb);
                cb.d2i();
                cb.swap();
                cb.checkcast(CD_ArrayValue);
                cb.swap();
                cb.invokeinterface(CD_ArrayValue, "get", MethodTypeDesc.of(CD_Value, CD_int));
                cb.labelBinding(endLabel);
            }
        }
        return false;
    }
}
