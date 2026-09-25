package tech.thatgravyboat.repolib.v2.expl.expression;

import java.util.Collection;
import java.util.stream.Collectors;
import org.jetbrains.annotations.NotNull;
import tech.thatgravyboat.repolib.v2.binary.BinaryCodec;
import tech.thatgravyboat.repolib.v2.binary.ExpressionTypeRegistry;
import tech.thatgravyboat.repolib.v2.binary.ExpressionTypes;
import tech.thatgravyboat.repolib.v2.expl.Evaluator;
import tech.thatgravyboat.repolib.v2.expl.value.Value;

public record BlockExpression(Collection<Expression<?>> exprs) implements Expression<BlockExpression> {
    public static final BinaryCodec<BlockExpression> CODEC =
        BinaryCodec.EXPRESSION.collection().mapped(BlockExpression::new, BlockExpression::exprs);

    @Override
    public @NotNull String toString() {
        if (exprs.isEmpty()) {
            return "{}";
        }
        return "{" + exprs.stream().map(Expression::toString).collect(Collectors.joining("; ")) + "}";
    }

    @Override
    public ExpressionTypeRegistry.Type<BlockExpression> expressionId() {
        return ExpressionTypes.BLOCK;
    }

    @Override
    public Value evaluate(Evaluator evaluator) {
        Value last = Value.NIL;
        for (var expr : exprs()) {
            if (expr.canReturnValueBeReturned()) {
                last = evaluator.eval0(expr);
            } else {
                evaluator.eval0(expr);
            }
        }

        return last;
    }

    public record LastElement(Expression<?> expression) implements Expression<LastElement> {
        public static final BinaryCodec<LastElement> CODEC =
            BinaryCodec.EXPRESSION.mapped(LastElement::new, LastElement::expression);

        @Override
        public Value evaluate(Evaluator evaluator) {
            return evaluator.eval0(this.expression);
        }

        @Override
        public boolean canReturnValueBeReturned() {
            return true;
        }

        @Override
        public ExpressionTypeRegistry.Type<LastElement> expressionId() {
            return ExpressionTypes.BLOCK_LAST_ELEMENT;
        }

    }

    @Override
    public boolean canReturnValueBeReturned() {
        return true;
    }
}
