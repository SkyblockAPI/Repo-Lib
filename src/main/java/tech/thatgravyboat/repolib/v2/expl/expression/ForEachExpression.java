package tech.thatgravyboat.repolib.v2.expl.expression;

import org.jetbrains.annotations.NotNull;
import tech.thatgravyboat.repolib.v2.expl.Evaluator;
import tech.thatgravyboat.repolib.v2.expl.ExecutionExceptions;
import tech.thatgravyboat.repolib.v2.expl.value.NilValue;
import tech.thatgravyboat.repolib.v2.expl.value.Value;

public record ForEachExpression(AccessExpression field, Expression array, Expression body) implements SelfEvaluatingExpression {
    @Override
    public Value evaluate(Evaluator evaluator) {
        var values = evaluator.getArrayOrThrow(evaluator.eval0(array));

        try {
            values.forEach(value -> {
                try {
                    evaluator.pushPop(this.toString(), () -> {
                        evaluator.eval0(new AssignExpression(field, new ValueExpression(value)));
                        return evaluator.eval0(body);
                    });
                } catch (ExecutionExceptions.Continue ignored) {}
            });
        } catch (ExecutionExceptions.Break ignored) {}
        return NilValue.NIL;
    }

    @Override
    public @NotNull String toString() {
        return "for (%s : %s) %s".formatted(
            field,
            array,
            body
        );
    }
    @Override
    public boolean requiresSemicolon() {
        return false;
    }
}
