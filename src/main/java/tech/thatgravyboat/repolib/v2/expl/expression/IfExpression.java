package tech.thatgravyboat.repolib.v2.expl.expression;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import tech.thatgravyboat.repolib.v2.binary.ByteBufferImpl;
import tech.thatgravyboat.repolib.v2.binary.DecoderContext;
import tech.thatgravyboat.repolib.v2.binary.EncoderContext;
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

import static java.lang.constant.ConstantDescs.CD_boolean;
import static tech.thatgravyboat.repolib.v2.jvm.compiler.ExplCD.CD_Evaluator;
import static tech.thatgravyboat.repolib.v2.jvm.compiler.ExplCD.CD_Value;

public record IfExpression(Expression cond, Expression thenExpr, @Nullable Expression elseExpr) implements Expression {

    @Override
    public @NotNull String toString() {
        if (elseExpr == null) {
            return String.format("if (%s) %s", cond, thenExpr);
        }
        return String.format("if (%s) %s else %s", cond, thenExpr, elseExpr);
    }

    @Override
    public boolean requiresSemicolon() {
        return false;
    }

    @Override
    public ExpressionTypeRegistry.Type<?> expressionId() {
        return ExpressionTypes.IF;
    }

    @Override
    public void precode(NameTable table) {
        table.insert(this.cond);
        table.insert(this.thenExpr);
        table.insert(this.elseExpr);
    }

    @Override
    public void encode(EncoderContext buffer) {
        ExpressionCodec.write(this.cond, buffer);
        ExpressionCodec.write(this.thenExpr, buffer);
        ExpressionCodec.writeNullable(this.elseExpr, buffer);
    }

    public static IfExpression decode(DecoderContext buffer) throws IOException {
        return new IfExpression(
                ExpressionCodec.read(buffer),
                ExpressionCodec.read(buffer),
                ExpressionCodec.readNullable(buffer)
        );
    }

    @Override
    public boolean compile(CodeBuilder cb, CompilationTracker lc) {
        cb.aload(1);
        cond.compile(cb, lc);
        Label endLabel = cb.newLabel();
        Label endEndLabel = cb.newLabel();
        cb.invokevirtual(CD_Evaluator, "asBool", MethodTypeDesc.of(CD_boolean, CD_Value));
        cb.ifeq(endLabel);
        thenExpr.compile(cb, lc);
        cb.goto_(endEndLabel);
        cb.labelBinding(endLabel);
        if (elseExpr != null) {
            elseExpr.compile(cb, lc);
        } else {
            Snippets.pushNil(cb);
        }
        cb.labelBinding(endEndLabel);
        cb.nop();
        return false;
    }
}
