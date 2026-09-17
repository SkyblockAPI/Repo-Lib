package tech.thatgravyboat.repolib.v2.expl.expression;

import java.io.IOException;
import java.lang.classfile.CodeBuilder;
import java.lang.reflect.InvocationTargetException;
import java.util.Collection;

import tech.thatgravyboat.repolib.v2.binary.ByteBuffer;
import tech.thatgravyboat.repolib.v2.binary.Encodable;
import tech.thatgravyboat.repolib.v2.binary.ExpressionCodec;
import tech.thatgravyboat.repolib.v2.binary.ExpressionTypeRegistry;
import tech.thatgravyboat.repolib.v2.binary.ExpressionTypes;
import tech.thatgravyboat.repolib.v2.expl.Evaluator;
import tech.thatgravyboat.repolib.v2.expl.value.FunctionValue;
import tech.thatgravyboat.repolib.v2.expl.value.Value;
import tech.thatgravyboat.repolib.v2.jvm.compiler.CompilationTracker;
import tech.thatgravyboat.repolib.v2.jvm.compiler.ExpressionCompiler;

import static tech.thatgravyboat.repolib.v2.jvm.compiler.ExplCD.CD_FunctionValue;

public record LambdaExpression(
        Collection<LambdaArgument> arguments, Expression body, Value function, boolean requiresSemicolon
) implements SelfEvaluatingExpression {

    public LambdaExpression(Collection<LambdaArgument> arguments, Expression body) {
        this(arguments, body, null);
    }

    public LambdaExpression(Collection<LambdaArgument> arguments, Expression body, Value self) {
        this(
            arguments, body, FunctionValue.builder(builder -> {
                int min = 0;
                int max = 0;
                boolean hasEncounteredOptional = false;

                for (var argument : arguments) {
                    max++;

                    if (argument.optional) {
                        hasEncounteredOptional = true;
                        continue;
                    }

                    if (hasEncounteredOptional) {
                        throw new IllegalStateException("Optional before required argument!");
                    }

                    min++;
                }

                if (min != max) {
                    builder.arity(min, max);
                } else {
                    builder.arity(min);
                }

                builder.execute((evaluator, values) -> evaluator.pushPop(
                    "lambda", () -> {
                        if (self != null) {
                            evaluator.set("self", self);
                        }

                        for (var argument : arguments) {
                            if (argument.optional() && argument.position() >= values.size()) continue;
                            evaluator.set(argument.name, values.get(argument.position));
                        }

                        return evaluator.evaluate(body);
                    }));
            }), body.requiresSemicolon());
    }


    @Override
    public Value evaluate(Evaluator _evaluator) {
        return function;
    }

    @Override
    public boolean canReturnValueBeReturned() {
        return true;
    }

    @Override
    public void encode(ByteBuffer buffer) {
        buffer.writeCollection(this.arguments, LambdaArgument::encode);
        ExpressionCodec.write(this.body, buffer);
        buffer.writeBoolean(this.requiresSemicolon);
    }

    @Override
    public ExpressionTypeRegistry.Type<?> expressionId() {
        return ExpressionTypes.LAMBDA;
    }

    public static LambdaExpression decode(ByteBuffer buffer) throws IOException {
        return new LambdaExpression(
                buffer.readCollection(LambdaArgument::decode),
                ExpressionCodec.read(buffer),
                null,
                buffer.readBoolean()
        );
    }

    @Override
    public boolean compile(CodeBuilder cb, CompilationTracker lc) {
        try {
            Class<?> lambdaClass = ExpressionCompiler.compileLambda(this, lc.getCodeName() + "$" + lc.uniqueId(), false);
            lc.loadTrackedObject(cb, lc.addTrackedObject(lambdaClass.getConstructor().newInstance()));
            cb.checkcast(CD_FunctionValue);
        } catch (IllegalAccessException | InstantiationException | InvocationTargetException | NoSuchMethodException | NoSuchFieldException e) {
            throw new RuntimeException(e);
        }
        return false;
    }

    public record LambdaArgument(String name, int position, boolean optional) implements Encodable {
        @Override
        public void encode(ByteBuffer buffer) {
            buffer.writeString(this.name);
            buffer.writeInt(this.position);
            buffer.writeBoolean(this.optional);
        }

        public static LambdaArgument decode(ByteBuffer buffer) throws IOException {
            return new LambdaArgument(
                    buffer.readString(),
                    buffer.readInt(),
                    buffer.readBoolean()
            );
        }
    }
}
