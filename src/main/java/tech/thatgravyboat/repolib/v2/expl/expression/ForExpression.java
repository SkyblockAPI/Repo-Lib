package tech.thatgravyboat.repolib.v2.expl.expression;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import tech.thatgravyboat.repolib.v2.binary.BinaryCodec;
import tech.thatgravyboat.repolib.v2.binary.BinaryRecordBuilder;
import tech.thatgravyboat.repolib.v2.binary.ExpressionTypeRegistry;
import tech.thatgravyboat.repolib.v2.binary.ExpressionTypes;
import tech.thatgravyboat.repolib.v2.expl.Evaluator;
import tech.thatgravyboat.repolib.v2.expl.ExecutionExceptions;
import tech.thatgravyboat.repolib.v2.expl.value.Value;

public record ForExpression(
    @Nullable Expression<?> init, @Nullable Expression<?> cond, @Nullable Expression<?> incr, Expression<?> body
) implements Expression<ForExpression> {

    public static final BinaryCodec<ForExpression> CODEC = BinaryRecordBuilder.of(
        BinaryCodec.EXPRESSION.nullable().forGetter(ForExpression::init),
        BinaryCodec.EXPRESSION.nullable().forGetter(ForExpression::cond),
        BinaryCodec.EXPRESSION.nullable().forGetter(ForExpression::incr),
        BinaryCodec.EXPRESSION.forGetter(ForExpression::body),
        ForExpression::new);

    @Override
    public @NotNull String toString() {
        return "for (%s;%s;%s) %s".formatted(
            init == null ? "" : init,
            cond == null ? "" : cond,
            incr == null ? "" : incr,
            body);
    }

    @Override
    public boolean requiresSemicolon() {
        return false;
    }


    @Override
    public ExpressionTypeRegistry.Type<ForExpression> expressionId() {
        return ExpressionTypes.FOR;
    }

    @Override
    public Value evaluate(Evaluator evaluator) {
            var init = init();
            if (init != null) {
                evaluator.eval0(init);
            }

            int iteration = 0;

            while (true) {
                if (iteration > Evaluator.MAX_ITERATIONS) {
                    throw new Evaluator.Panic("For loop has iterated more than %d times, aborting to prevent infinite loop.".formatted(Evaluator.MAX_ITERATIONS));
                }

                var cond = cond();
                if (cond != null && !evaluator.eval0(cond).asBooleanConversion()) {
                    break;
                }

                try {
                    evaluator.pushPop("for (...; " + cond() + "; ...)", () -> evaluator.eval0(body()));
                } catch (ExecutionExceptions.Break e) {
                    break;
                } catch (ExecutionExceptions.Continue e) {
                    // do nothing, just continue to the next iteration.
                }

                var incr = incr();
                if (incr != null) {
                    evaluator.eval0(incr);
                }

                iteration++;
            }

            return Value.NIL;
    }
}
