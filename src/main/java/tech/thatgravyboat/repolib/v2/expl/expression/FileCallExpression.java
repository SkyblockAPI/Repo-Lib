package tech.thatgravyboat.repolib.v2.expl.expression;


import org.jetbrains.annotations.NotNull;
import tech.thatgravyboat.repolib.v2.binary.ByteBuffer;
import tech.thatgravyboat.repolib.v2.binary.ExpressionCodec;
import tech.thatgravyboat.repolib.v2.binary.ExpressionTypeRegistry;
import tech.thatgravyboat.repolib.v2.binary.ExpressionTypes;
import tech.thatgravyboat.repolib.v2.expl.Evaluator;
import tech.thatgravyboat.repolib.v2.expl.value.Value;
import tech.thatgravyboat.repolib.v2.jvm.compiler.CompilationTracker;

import java.io.IOException;
import java.lang.classfile.CodeBuilder;
import java.lang.constant.MethodTypeDesc;

import static tech.thatgravyboat.repolib.v2.jvm.compiler.ExplCD.CD_Evaluator;
import static tech.thatgravyboat.repolib.v2.jvm.compiler.ExplCD.CD_StructValue;
import static tech.thatgravyboat.repolib.v2.jvm.compiler.ExplCD.CD_StructuredFunctionValue;
import static tech.thatgravyboat.repolib.v2.jvm.compiler.ExplCD.CD_Value;

public record FileCallExpression(Expression access, StructExpression expr) implements SelfEvaluatingExpression {

    @Override
    public @NotNull String toString() {
        return access + " " + expr;
    }

    @Override
    public Value evaluate(Evaluator evaluator) {
        var function = evaluator.getStructuredFunctionOrThrow(evaluator.eval0(access));
        var structValue = evaluator.evalStruct(expr);

        return function.apply(evaluator, structValue);
    }

    @Override
    public ExpressionTypeRegistry.Type<?> expressionId() {
        return ExpressionTypes.FILE_CALL;
    }

    @Override
    public void encode(ByteBuffer buffer) {
        ExpressionCodec.write(this.access, buffer);
        ExpressionCodec.writeUntyped(this.expr, buffer);
    }

    public static FileCallExpression decode(ByteBuffer buffer) throws IOException {
        return new FileCallExpression(
                ExpressionCodec.read(buffer),
                ExpressionCodec.readUntyped(ExpressionTypes.STRUCT, buffer)
        );
    }

    @Override
    public boolean compile(CodeBuilder cb, CompilationTracker lc) {
        access.compile(cb, lc);
        cb.checkcast(CD_StructuredFunctionValue);
        cb.aload(1);
        expr.compile(cb, lc);
        cb.invokeinterface(CD_StructuredFunctionValue, "apply", MethodTypeDesc.of(CD_Value, CD_Evaluator, CD_StructValue));
        return false;
    }
}

