package tech.thatgravyboat.repolib.v2.expl.expression;

import java.util.Collection;
import tech.thatgravyboat.repolib.v2.binary.BinaryCodec;
import tech.thatgravyboat.repolib.v2.binary.BinaryRecordBuilder;
import tech.thatgravyboat.repolib.v2.binary.ExpressionTypeRegistry;
import tech.thatgravyboat.repolib.v2.binary.ExpressionTypes;
import tech.thatgravyboat.repolib.v2.expl.Evaluator;
import tech.thatgravyboat.repolib.v2.expl.value.FunctionValue;
import tech.thatgravyboat.repolib.v2.expl.value.Value;

public record LambdaExpression(
    Collection<LambdaArgument> arguments, Expression<?> body, Value function, boolean requiresSemicolon
) implements Expression<LambdaExpression> {

    public static final BinaryCodec<LambdaExpression> CODEC = BinaryRecordBuilder.of(
        LambdaArgument.CODEC.collection().forGetter(LambdaExpression::arguments),
        BinaryCodec.EXPRESSION.forGetter(LambdaExpression::body),
        LambdaExpression::new);

    public LambdaExpression(Collection<LambdaArgument> arguments, Expression<?> body) {
        this(arguments, body, null);
    }

    public LambdaExpression(Collection<LambdaArgument> arguments, Expression<?> body, Value self) {
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
                            if (argument.optional() && argument.position() >= values.size()) {
                                continue;
                            }
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
    public ExpressionTypeRegistry.Type<LambdaExpression> expressionId() {
        return ExpressionTypes.LAMBDA;
    }

    public record LambdaArgument(String name, int position, boolean optional) {
        public static final BinaryCodec<LambdaArgument> CODEC = BinaryRecordBuilder.of(
            BinaryCodec.STRING.forGetter(LambdaArgument::name),
            BinaryCodec.INT.forGetter(LambdaArgument::position),
            BinaryCodec.BOOLEAN.forGetter(LambdaArgument::optional),
            LambdaArgument::new);

    }
}
