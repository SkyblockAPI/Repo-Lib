package tech.thatgravyboat.repolib.v2.expl.expression;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import tech.thatgravyboat.repolib.v2.binary.ByteBufferImpl;
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
import java.lang.constant.MethodTypeDesc;

import static java.lang.constant.ConstantDescs.CD_boolean;
import static tech.thatgravyboat.repolib.v2.jvm.compiler.ExplCD.CD_Evaluator;
import static tech.thatgravyboat.repolib.v2.jvm.compiler.ExplCD.CD_Value;

public record ForExpression(@Nullable Expression init, @Nullable Expression cond, @Nullable Expression incr, Expression body) implements Expression {

    @Override
    public @NotNull String toString() {
        return "for (%s;%s;%s) %s".formatted(
                init == null ? "" : init,
                cond == null ? "" : cond,
                incr == null ? "" : incr,
                body
        );
    }

    @Override
    public boolean requiresSemicolon() {
        return false;
    }


    @Override
    public ExpressionTypeRegistry.Type<?> expressionId() {
        return ExpressionTypes.FOR;
    }

    @Override
    public void precode(NameTable table) {
        table.insert(this.init);
        table.insert(this.cond);
        table.insert(this.incr);
        table.insert(this.body);
    }

    @Override
    public void encode(EncoderContext buffer) {
        ExpressionCodec.writeNullable(this.init, buffer);
        ExpressionCodec.writeNullable(this.cond, buffer);
        ExpressionCodec.writeNullable(this.incr, buffer);
        ExpressionCodec.write(this.body, buffer);
    }

    public static ForExpression decode(DecoderContext buffer) throws IOException {
        return new ForExpression(
                ExpressionCodec.readNullable(buffer),
                ExpressionCodec.readNullable(buffer),
                ExpressionCodec.readNullable(buffer),
                ExpressionCodec.read(buffer)
        );
    }

    @Override
    public boolean compile(CodeBuilder cb, CompilationTracker lc) {
        Label endLoopLabel = cb.newLabel();
        Label loopLabel = cb.newLabel();
        Label checkLabel = cb.newLabel();

        Label oldBreakLabel = lc.getBreakLabel();
        Label oldContinueLabel = lc.getContinueLabel();
        lc.setBreakLabel(endLoopLabel);
        lc.setContinueLabel(loopLabel);

        if (init != null) {
            init.compile(cb, lc);
            cb.pop();
        }
        cb.goto_(checkLabel);
        cb.labelBinding(loopLabel);
        if (incr != null) {
            incr.compile(cb, lc);
            cb.pop();
        }
        cb.labelBinding(checkLabel);
        if (cond != null) {
            cb.aload(1);
            cond.compile(cb, lc);
            cb.invokevirtual(CD_Evaluator, "asBool", MethodTypeDesc.of(CD_boolean, CD_Value));
            cb.ifeq(endLoopLabel);
        }

        body.compile(cb, lc);
        cb.pop();

        cb.goto_(loopLabel);
        cb.labelBinding(endLoopLabel);
        Snippets.pushNil(cb);
        lc.setBreakLabel(oldBreakLabel);
        lc.setBreakLabel(oldContinueLabel);
        return false;
    }
}
