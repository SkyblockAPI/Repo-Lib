package tech.thatgravyboat.repolib.v2.expl.expression;

import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.stream.Collectors;
import tech.thatgravyboat.repolib.v2.expl.Evaluator;
import tech.thatgravyboat.repolib.v2.expl.value.Value;

public record BlockExpression(List<Expression> exprs) implements Expression {

    @Override
    public @NotNull String toString() {
        if (exprs.isEmpty()) {
            return "{}";
        }
        return "{" + exprs.stream().map(Expression::toString).collect(Collectors.joining("; ")) + "}";
    }

    public record LastElement(Expression expression) implements SelfEvaluatingExpression {
        @Override
        public Value evaluate(Evaluator evaluator) {
            return evaluator.eval0(this.expression);
        }

        @Override
        public boolean canReturnValueBeReturned() {
            return true;
        }
    }

    @Override
    public boolean canReturnValueBeReturned() {
        return true;
    }
}
