package tech.thatgravyboat.repolib.v2.expl.expression;

import tech.thatgravyboat.repolib.v2.binary.DecoderContext;
import tech.thatgravyboat.repolib.v2.binary.EncoderContext;
import tech.thatgravyboat.repolib.v2.binary.ExpressionCodec;
import tech.thatgravyboat.repolib.v2.binary.ExpressionTypeRegistry;
import tech.thatgravyboat.repolib.v2.binary.ExpressionTypes;
import tech.thatgravyboat.repolib.v2.binary.NameTable;
import tech.thatgravyboat.repolib.v2.expl.Evaluator;
import tech.thatgravyboat.repolib.v2.expl.ExecutionExceptions;
import tech.thatgravyboat.repolib.v2.expl.value.Value;
import tech.thatgravyboat.repolib.v2.jvm.compiler.CompilationTracker;

import java.io.IOException;
import java.lang.classfile.CodeBuilder;

public record ReturnExpression(Expression retExpr) implements SelfEvaluatingExpression {
    @Override
    public Value evaluate(Evaluator evaluator) {
        throw new ExecutionExceptions.Return(evaluator.eval0(retExpr));
    }

    @Override
    public ExpressionTypeRegistry.Type<?> expressionId() {
        return ExpressionTypes.RETURN;
    }

    @Override
    public void precode(NameTable table) {
        table.insert(this.retExpr);
    }

    @Override
    public void encode(EncoderContext buffer) {
        ExpressionCodec.write(this.retExpr, buffer);
    }

    public static ReturnExpression decode(DecoderContext buffer) throws IOException {
        return new ReturnExpression(ExpressionCodec.read(buffer));
    }

    @Override
    public boolean compile(CodeBuilder cb, CompilationTracker lc) {
        retExpr.compile(cb, lc);
        lc.popAll(cb);
        cb.areturn();
        return true;
    }
}
