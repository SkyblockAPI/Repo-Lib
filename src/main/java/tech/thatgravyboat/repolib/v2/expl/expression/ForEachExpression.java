package tech.thatgravyboat.repolib.v2.expl.expression;

import org.jetbrains.annotations.NotNull;
import tech.thatgravyboat.repolib.v2.expl.Evaluator;
import tech.thatgravyboat.repolib.v2.expl.value.Value;

public record ForEachExpression(AccessExpression field, Expression array, Expression body) implements SelfEvaluatingExpression {
    @Override
    public Value evaluate(Evaluator evaluator) {
        var values = evaluator.getArrayOrThrow(evaluator.eval0(array));

        values.forEach(value -> evaluator.pushPop(this.toString(), () -> {
            evaluator.eval0(new AssignExpression(field, new ValueExpression(value)));
            return evaluator.eval0(body);
        }));
        return null;
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
