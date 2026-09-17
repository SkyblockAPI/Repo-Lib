package tech.thatgravyboat.repolib.v2.expl.expression;

import java.io.IOException;
import java.util.Collection;

import tech.thatgravyboat.repolib.v2.binary.ByteBufferImpl;
import tech.thatgravyboat.repolib.v2.binary.DecoderContext;
import tech.thatgravyboat.repolib.v2.binary.Encodable;
import tech.thatgravyboat.repolib.v2.binary.EncoderContext;
import tech.thatgravyboat.repolib.v2.binary.ExpressionCodec;
import tech.thatgravyboat.repolib.v2.binary.ExpressionTypeRegistry;
import tech.thatgravyboat.repolib.v2.binary.ExpressionTypes;
import tech.thatgravyboat.repolib.v2.binary.NameTable;
import tech.thatgravyboat.repolib.v2.expl.Evaluator;
import tech.thatgravyboat.repolib.v2.expl.value.FunctionValue;
import tech.thatgravyboat.repolib.v2.expl.value.Value;

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
    public void encode(EncoderContext buffer) {
        buffer.writeCollection(this.arguments, LambdaArgument::encode);
        ExpressionCodec.write(this.body, buffer);
        buffer.writeBoolean(this.requiresSemicolon);
    }

    @Override
    public ExpressionTypeRegistry.Type<?> expressionId() {
        return ExpressionTypes.LAMBDA;
    }

    public static LambdaExpression decode(DecoderContext buffer) throws IOException {
        return new LambdaExpression(
                buffer.readCollection(LambdaArgument::decode),
                ExpressionCodec.read(buffer),
                null,
                buffer.readBoolean()
        );
    }

    @Override
    public void precode(NameTable table) {
        for (var argument : this.arguments) {
            table.insert(argument);
        }
        table.insert(this.body);
    }

    public record LambdaArgument(String name, int position, boolean optional) implements Encodable {
        @Override
        public void encode(EncoderContext buffer) {
            buffer.writeLiteral(this.name);
            buffer.writeInt(this.position);
            buffer.writeBoolean(this.optional);
        }

        @Override
        public void precode(NameTable table) {
            table.insert(this.name);
        }

        public static LambdaArgument decode(DecoderContext buffer) throws IOException {
            return new LambdaArgument(
                    buffer.readLiteral(),
                    buffer.readInt(),
                    buffer.readBoolean()
            );
        }
    }
}
