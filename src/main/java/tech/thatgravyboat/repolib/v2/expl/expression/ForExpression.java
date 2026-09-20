package tech.thatgravyboat.repolib.v2.expl.expression;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
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

        lc.pushStack(cb, "for");

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
            cond.compileBoolean(cb, lc);
            cb.ifeq(endLoopLabel);
        }

        body.compile(cb, lc);
        cb.pop();

        cb.goto_(loopLabel);
        cb.labelBinding(endLoopLabel);
        Snippets.pushNil(cb);
        lc.popStack(cb);
        lc.setBreakLabel(oldBreakLabel);
        lc.setContinueLabel(oldContinueLabel);
        return false;
    }
}
