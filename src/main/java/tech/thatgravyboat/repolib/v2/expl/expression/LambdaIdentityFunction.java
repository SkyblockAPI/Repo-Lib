package tech.thatgravyboat.repolib.v2.expl.expression;

import java.io.IOException;
import java.lang.classfile.CodeBuilder;
import java.lang.constant.MethodTypeDesc;

import tech.thatgravyboat.repolib.v2.binary.ByteBuffer;
import tech.thatgravyboat.repolib.v2.binary.ExpressionCodec;
import tech.thatgravyboat.repolib.v2.binary.ExpressionTypeRegistry;
import tech.thatgravyboat.repolib.v2.binary.ExpressionTypes;
import tech.thatgravyboat.repolib.v2.expl.Evaluator;
import tech.thatgravyboat.repolib.v2.expl.value.Value;
import tech.thatgravyboat.repolib.v2.jvm.compiler.CompilationTracker;
import tech.thatgravyboat.repolib.v2.jvm.compiler.ExpressionCompiler;

import static java.lang.constant.ConstantDescs.*;
import static tech.thatgravyboat.repolib.v2.jvm.compiler.ExplCD.CD_IdentityLambdaFunctionValue;
import static tech.thatgravyboat.repolib.v2.jvm.compiler.ExplCD.CD_Value;

public record LambdaIdentityFunction(LambdaExpression expression) implements SelfEvaluatingExpression {
    @Override
    public Value evaluate(Evaluator evaluator) {
        return null;
    }

    @Override
    public ExpressionTypeRegistry.Type<?> expressionId() {
        return ExpressionTypes.LAMBDA_IDENTITY;
    }

    @Override
    public void encode(ByteBuffer buffer) {
        ExpressionCodec.writeUntyped(this.expression, buffer);
    }

    public static LambdaIdentityFunction decode(ByteBuffer buffer) throws IOException {
        return new LambdaIdentityFunction(ExpressionCodec.readUntyped(ExpressionTypes.LAMBDA, buffer));
    }

    @Override
    public boolean compile(CodeBuilder cb, CompilationTracker lc) {
        try {
            Class<?> lambdaClass = ExpressionCompiler.compileLambda(expression, lc.getCodeName() + "$" + lc.uniqueId(), true);
            lc.loadTrackedObject(cb, lc.addTrackedObject(lambdaClass));
            cb.checkcast(CD_Class);
            cb.invokevirtual(CD_Class, "newInstance", MethodTypeDesc.of(CD_Object));
            cb.checkcast(CD_IdentityLambdaFunctionValue);
            cb.swap();
            cb.invokeinterface(CD_IdentityLambdaFunctionValue, "setSelf", MethodTypeDesc.of(CD_Value, CD_Value));
        } catch (IllegalAccessException | InstantiationException | NoSuchFieldException e) {
            throw new RuntimeException(e);
        }
        return false;
    }
}
